package skullbeware

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.html.Audio

object MusicPlayer {

  val musicVolume: Double   = 0.5
  val effectsVolume: Double = 0.7

  var currentSong: Option[html.Audio] = None

  def play() = {
    val song = newAudio
    song.src = assetPath("music.mp3")
    song.play()
    song.volume = musicVolume
    song.loop = true
    currentSong = Some(song)
  }

  def newAudio: Audio = {
    dom.document.createElement("AUDIO").asInstanceOf[Audio]
  }

  def reset() = {
    stop()
    play()
  }

  def stop() = {
    currentSong match {
      case Some(song) => song.pause()
      case _          => // nothing
    }
  }

  def playEffect(name: String) = {
    val track = newAudio
    track.volume = effectsVolume
    track.src = name
    track.play()
  }
}
