package com.example.flopractice

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.flopractice.databinding.ActivitySongBinding

class SongActivity : AppCompatActivity() {


    lateinit var binding: ActivitySongBinding
    lateinit var song: Song
    lateinit var timer: Timer
    private var mediaPlayer : MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initSong()
        setPlayer(song)


        binding.songDownIb.setOnClickListener {
            finish()
        }

        binding.songMiniplayerIv.setOnClickListener {
            setPlayerStatus(false)
        }

        binding.songPauseIv.setOnClickListener {
            setPlayerStatus(true)
        }


    }

    private fun initSong() {
        if (intent.hasExtra("title") && intent.hasExtra("singer")) {
            song = Song(
                intent.getStringExtra("title")!!,
                intent.getStringExtra("singer")!!,
                intent.getIntExtra("second", 0),
                intent.getIntExtra("playTime", 0),
                intent.getBooleanExtra("isPlaying", false),
                intent.getStringExtra("music")!!
            )
        }
        startTimer()
    }



    private fun setPlayer(song: Song) {
        binding.songMusicTitleTv.text = intent.getStringExtra("title")
        binding.songSingerNameTv.text = intent.getStringExtra("singer")
        binding.songStartTimeTv.text =
            String.format("%02d:%02d", song.second / 60, song.second % 60)

        binding.songEndTimeTv.text =
            String.format("%02d:%02d", song.second / 60, song.second % 60)

        binding.songProgressSb.progress =
            (song.second * 1000 / song.playTime)

        val music = resources.getIdentifier(song.music,"raw",this.packageName)
        mediaPlayer = MediaPlayer.create(this,music)

        setPlayerStatus(song.isPlaying)
    }


    private fun setPlayerStatus(isPlaying: Boolean) {

        song.isPlaying = isPlaying
        timer.isplaying = isPlaying


        if (isPlaying) {
            binding.songMiniplayerIv.visibility = View.VISIBLE
            binding.songPauseIv.visibility = View.GONE
            mediaPlayer?.start()

        } else {

            binding.songMiniplayerIv.visibility = View.GONE
            binding.songPauseIv.visibility = View.VISIBLE
            if (mediaPlayer?.isPlaying == true){
                mediaPlayer?.pause()
            }
        }


    }

    private fun startTimer() {
        timer = Timer(song.playTime, song.isPlaying)
        timer.start()


    }

    inner class Timer(private val playTime: Int, var isplaying: Boolean = true) : Thread() {

        private var second: Int = 0
        private var mills: Float = 0f

        override fun run() {
            super.run()
            try {
                while (true) {
                    if (second >= playTime) {
                        break
                    }

                    if (isplaying) {
                        sleep(50)
                        mills += 50
                        runOnUiThread {

                            binding.songProgressSb.progress = ((mills / playTime) * 100).toInt()

                        }
                        if (mills % 1000 == 0f) {
                            runOnUiThread {
                                binding.songStartTimeTv.text =
                                    String.format("%02d:%02d", second / 60, second % 60)
                            }

                            second++

                        }

                    }
                }

            } catch (e: InterruptedException) {

                Log.d("Song","쓰레드가 죽었습니다${e.message}")

            }


        }
    }

    // 사용자가 포커스를 잃었을 때 음악이 중지
    override fun onPause() {
        super.onPause()
        setPlayerStatus(false)

    }

    override fun onDestroy() {
        super.onDestroy()
        timer.interrupt()
        mediaPlayer?.release() // 미디어가 갖고 있던 리소스 해제
    }
}