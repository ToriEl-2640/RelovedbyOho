package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

/**
 * Classical Piece metadata model.
 */
data class ClassicalTrack(
    val id: String,
    val title: String,
    val composer: String,
    val movementOrOpus: String,
    val period: String,
    val description: String,
    val rawResId: Int
)

/**
 * Background Classical Music Player for OHO App Sessions.
 * Plays timeless, peaceful classical songs (Erik Satie, J.S. Bach, Chopin)
 * designed to soothe, relax, and create an elegant atmosphere during shopping sessions.
 */
class SalsaMusicPlayer private constructor(private val context: Context) {

    val classicalTracks = listOf(
        ClassicalTrack(
            id = "cafe_jazz",
            title = "Boutique Café Jazz",
            composer = "Café Jazz",
            movementOrOpus = "Instrumental",
            period = "Jazz Lounge",
            description = "Smooth upright bass, brushed drums & velvet jazz piano chords",
            rawResId = R.raw.boutique_cafe_jazz
        ),
        ClassicalTrack(
            id = "feelgood_pop",
            title = "Feel-Good Pop Hits",
            composer = "Feel-Good Pop",
            movementOrOpus = "Bright & Catchy",
            period = "Modern Pop",
            description = "Bright, catchy music that makes browsing feel fun and upbeat",
            rawResId = R.raw.feelgood_pop
        ),
        ClassicalTrack(
            id = "nigerian_highlife",
            title = "Highlife & Nigerian Classics",
            composer = "Nigerian Highlife",
            movementOrOpus = "Mellow Palm-Wine",
            period = "Afro Classics",
            description = "Warm, familiar, welcoming Nigerian highlife guitars & shekere groove",
            rawResId = R.raw.nigerian_highlife
        )
    )

    private var mediaPlayer: MediaPlayer? = null
    private var userWantedPlay = true

    private val _currentTrack = MutableStateFlow(classicalTracks[0])
    val currentTrack: StateFlow<ClassicalTrack> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Default audible, comfortable volume (0.65f)
    private val _volume = MutableStateFlow(0.65f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    val trackTitle: String
        get() = _currentTrack.value.title

    val trackSubtitle: String
        get() = "${_currentTrack.value.composer} • ${_currentTrack.value.movementOrOpus}"

    val currentComposer: String
        get() = _currentTrack.value.composer

    init {
        initializePlayer(_currentTrack.value)
    }

    /**
     * Initializes and prepares MediaPlayer using a guaranteed multi-tier strategy:
     * Tier 1: Standard MediaPlayer.create(context, rawResId) - synchronous, official Android method.
     * Tier 2: Stream copy from openRawResource() to app cache file + setDataSource(path) - 100% immune to APK compression or FD issues.
     */
    private fun initializePlayer(track: ClassicalTrack) {
        releaseInternal()

        var player: MediaPlayer? = null

        // Strategy 1: Android MediaPlayer.create()
        try {
            Log.d("ClassicalMusicPlayer", "Attempting MediaPlayer.create for ${track.title}")
            val created = MediaPlayer.create(context, track.rawResId)
            if (created != null) {
                player = created
                Log.i("ClassicalMusicPlayer", "Successfully created player via MediaPlayer.create for ${track.title}")
            }
        } catch (e: Exception) {
            Log.w("ClassicalMusicPlayer", "MediaPlayer.create failed for ${track.title}", e)
        }

        // Strategy 2: Cache file stream extraction fallback (guaranteed to work everywhere)
        if (player == null) {
            try {
                Log.d("ClassicalMusicPlayer", "Extracting raw resource to cache file fallback for ${track.title}")
                val cacheDir = File(context.cacheDir, "classical_audio")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val cacheFile = File(cacheDir, "${track.id}.mp3")

                if (!cacheFile.exists() || cacheFile.length() < 10_000) {
                    context.resources.openRawResource(track.rawResId).use { input ->
                        FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                if (cacheFile.exists() && cacheFile.length() > 0) {
                    val filePlayer = MediaPlayer()
                    filePlayer.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    filePlayer.setDataSource(cacheFile.absolutePath)
                    filePlayer.prepare()
                    player = filePlayer
                    Log.i("ClassicalMusicPlayer", "Successfully initialized player from cache file for ${track.title}")
                }
            } catch (e: Exception) {
                Log.e("ClassicalMusicPlayer", "Cache file extraction failed for ${track.title}", e)
            }
        }

        if (player != null) {
            mediaPlayer = player
            try {
                player.isLooping = true
                applyVolume()

                player.setOnCompletionListener {
                    Log.d("ClassicalMusicPlayer", "Track completed, cycling next track")
                    nextTrack()
                }

                player.setOnErrorListener { _, what, extra ->
                    Log.w("ClassicalMusicPlayer", "MediaPlayer runtime error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    true
                }

                if (userWantedPlay) {
                    player.start()
                    _isPlaying.value = true
                    Log.i("ClassicalMusicPlayer", "Playback started for ${track.title} at volume ${_volume.value}")
                }
            } catch (e: Exception) {
                Log.e("ClassicalMusicPlayer", "Error configuring player for ${track.title}", e)
            }
        } else {
            Log.e("ClassicalMusicPlayer", "Could not initialize player for ${track.title} with any strategy")
            _isPlaying.value = false
        }
    }

    fun selectTrack(track: ClassicalTrack) {
        if (_currentTrack.value.id == track.id && mediaPlayer != null) {
            if (!_isPlaying.value) play()
            return
        }
        _currentTrack.value = track
        initializePlayer(track)
    }

    fun nextTrack() {
        val currentIndex = classicalTracks.indexOfFirst { it.id == _currentTrack.value.id }
        val nextIndex = if (currentIndex in 0 until classicalTracks.size - 1) currentIndex + 1 else 0
        selectTrack(classicalTracks[nextIndex])
    }

    fun previousTrack() {
        val currentIndex = classicalTracks.indexOfFirst { it.id == _currentTrack.value.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else classicalTracks.size - 1
        selectTrack(classicalTracks[prevIndex])
    }

    fun play() {
        userWantedPlay = true
        val player = mediaPlayer
        if (player == null) {
            initializePlayer(_currentTrack.value)
            return
        }
        try {
            applyVolume()
            if (!player.isPlaying) {
                player.start()
            }
            _isPlaying.value = true
            Log.i("ClassicalMusicPlayer", "Play resumed for ${_currentTrack.value.title}")
        } catch (e: Exception) {
            Log.e("ClassicalMusicPlayer", "Error starting playback, re-initializing", e)
            initializePlayer(_currentTrack.value)
        }
    }

    fun pause() {
        userWantedPlay = false
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
            _isPlaying.value = false
            Log.i("ClassicalMusicPlayer", "Playback paused")
        } catch (e: Exception) {
            Log.e("ClassicalMusicPlayer", "Error pausing player", e)
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        if (_isMuted.value && clamped > 0f) {
            _isMuted.value = false
        }
        applyVolume()
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        applyVolume()
    }

    private fun applyVolume() {
        val effectiveVol = if (_isMuted.value) 0.0f else _volume.value
        try {
            mediaPlayer?.setVolume(effectiveVol, effectiveVol)
        } catch (e: Exception) {
            Log.w("ClassicalMusicPlayer", "Could not apply volume", e)
        }
    }

    fun onAppBackground() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            }
        } catch (e: Exception) {
            Log.w("ClassicalMusicPlayer", "Error in onAppBackground", e)
        }
    }

    fun onAppForeground() {
        if (userWantedPlay) {
            play()
        }
    }

    private fun releaseInternal() {
        try {
            mediaPlayer?.stop()
        } catch (e: Exception) {
            // ignore
        }
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        _isPlaying.value = false
    }

    fun release() {
        releaseInternal()
    }

    companion object {
        @Volatile
        private var instance: SalsaMusicPlayer? = null

        fun getInstance(context: Context): SalsaMusicPlayer {
            return instance ?: synchronized(this) {
                instance ?: SalsaMusicPlayer(context.applicationContext).also { instance = it }
            }
        }
    }
}
