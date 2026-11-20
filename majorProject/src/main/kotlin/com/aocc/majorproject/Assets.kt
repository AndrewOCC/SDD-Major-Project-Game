package com.aocc.majorproject

import android.graphics.Typeface
import com.aocc.framework.Image
import com.aocc.framework.Music
import com.aocc.framework.NoOpMusic
import com.aocc.framework.Sound

object Assets {

    private val MUSIC_FILES = arrayOf(
        "darude-sandstorm.m4a",
        "game-music.ogg",
    )

    @JvmField
    var noise: Image? = null

    @JvmField
    var menu_bg: Image? = null

    @JvmField
    var game_bg: Image? = null

    @JvmField
    var sign_in_base: Image? = null

    @JvmField
    var sign_in_press: Image? = null

    @JvmField
    var gpg_icon_leaderboards: Image? = null

    @JvmField
    var gpg_icon_achievements: Image? = null

    @JvmField
    var splash: Image? = null

    @JvmField
    var tilt_control_flat: Image? = null

    @JvmField
    var tilt_control_tilted: Image? = null

    @JvmField
    var tilt_control_custom: Image? = null

    @JvmField
    var tilt_control_flat_2: Image? = null

    @JvmField
    var tilt_control_tilted_2: Image? = null

    @JvmField
    var tilt_control_custom_2: Image? = null

    @JvmField
    var sound: Image? = null

    @JvmField
    var sound_muted: Image? = null

    @JvmField
    var music: Image? = null

    @JvmField
    var music_muted: Image? = null

    @JvmField
    var tutorial: Image? = null

    /** Game logo (lifted from the menu art) and frost particles for the dual-screen title. */
    @JvmField
    var logo: Image? = null

    @JvmField
    var flakes: List<Image> = emptyList()

    /** Pre-rendered apartment (tools/render/apartment.py) for the Roomba theme. */
    @JvmField
    var floor: Image? = null

    /** Every time of day of the room, for crossfading (see [com.aocc.majorproject.roomba.DayCycle]). */
    @JvmField
    var rooms: Map<com.aocc.majorproject.roomba.DayCycle.Scene, Image> = emptyMap()

    /** "Owner comes home" effects, keyed by name without the `home_` prefix (car, steps, keys...). */
    @JvmField
    var homeSounds: Map<String, Sound> = emptyMap()

    /** Roomba rotation sheet: 8x8 frames of [com.aocc.majorproject.roomba.RoombaRenderer.CELL] units. */
    @JvmField
    var roombaSheet: Image? = null

    /** Charging dock, one frame per wall (left, top, right, bottom). */
    @JvmField
    var dockSheet: Image? = null

    /** Gameplay geometry of the rendered room (walls, furniture, outlets). */
    @JvmField
    var roomLayout: com.aocc.majorproject.roomba.RoomLayout = com.aocc.majorproject.roomba.RoomLayout.EMPTY

    @JvmField
    var tap: Sound? = null

    @JvmField
    var zap: Sound? = null

    @JvmField
    var powerup: Sound? = null

    @JvmField
    var burn: Sound? = null

    @JvmField
    var darude: Music? = null

    @JvmField
    var plain: Typeface? = null

    @JvmField
    var bold: Typeface? = null

    /** Large display cut, used for the floor-projected HUD and headings. */
    @JvmField
    var display: Typeface? = null

    /** Pixel scale tier used when loading bitmaps (1 or 2). */
    @JvmField
    var loadedPixelScale: Int = 1

    @JvmStatic
    fun loadMusic(majorProjectGame: MajorProjectGame) {
        for (musicFile in MUSIC_FILES) {
            try {
                val music = majorProjectGame.audio.createMusic(musicFile)
                darude = music
                if (GamePreferences.music && !majorProjectGame.isMusicActive) {
                    music.setVolume(0.85f)
                    music.setLooping(true)
                    music.play()
                } else if (!GamePreferences.music) {
                    music.setVolume(0f)
                }
                return
            } catch (e: RuntimeException) {
                CrashReporter.log(majorProjectGame, "Failed to load music: $musicFile", e)
            }
        }

        darude = NoOpMusic()
    }

    @JvmStatic
    fun hasPlayableMusic(): Boolean {
        return darude != null && darude !is NoOpMusic
    }

    @JvmStatic
    fun setMusicVolume(volume: Float) {
        darude?.setVolume(volume)
    }

    @JvmStatic
    fun playMusic() {
        darude?.play()
    }

    @JvmStatic
    fun pauseMusic() {
        darude?.pause()
    }
}
