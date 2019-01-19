import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer

class AACEncoder {

    private var mBufferInfo: MediaCodec.BufferInfo? = null
    private var mEncoder: MediaCodec? = null
    private var mFileStream: OutputStream? = null
    private val sampleRate = 44100 // 44.1[KHz] is only setting guaranteed to be
    //available on all devices.
    private val bitrate = 64000
    private val channelCount = 1

    //下面的都是用来回调编码后的数据
    private var encoderListener: EncoderListener? = null

    init {
        initEncoder()
    }

    /**
     * 添加ADTS头
     */
    private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
        val profile = 2 //AAC LC
        val freqIdx = 4 //44100->4 根据不同的采样率修改这个值 48000->3
        val chanCfg = 2 //CPE
        packet[0] = 0xFF.toByte()
        packet[1] = 0xF9.toByte()
        packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }

    /**
     * 释放资源
     */
    fun release() {
        if (mFileStream != null) {
            try {
                mFileStream!!.flush()
                mFileStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        if (mEncoder != null) {
            mEncoder!!.stop()
        }
    }

    /**
     * 编码
     */
    fun encode(buffer: ByteBuffer, size: Int) {
        // 取出InputBuffer，填充音频数据，然后输送到编码器进行编码
        val inputBufferIndex = mEncoder!!.dequeueInputBuffer(1000)
        if (inputBufferIndex >= 0) {
            //ByteBuffer inputBuffer = mEncorder.getInputBuffer(inputBufferIndex);
            val inputBuffers = mEncoder!!.inputBuffers
            val inputBuffer = inputBuffers[inputBufferIndex]
            inputBuffer.clear()

            inputBuffer.put(buffer)
            mEncoder!!.queueInputBuffer(inputBufferIndex, 0, size,
                    System.nanoTime(), 0)
        }

        // 取出编码好的一帧音频数据，然后给这一帧添加ADTS头
        var outputBufferIndex = mEncoder!!.dequeueOutputBuffer(mBufferInfo!!, 1000)
        val outputBuffers = mEncoder!!.outputBuffers

        while (outputBufferIndex >= 0) {
            val outBitsSize = mBufferInfo!!.size
            val outPacketSize = outBitsSize + 7 // ADTS头部是7个字节
            //ByteBuffer outputBuffer = mEncorder.getOutputBuffer(outputBufferIndex);
            val outputBuffer = outputBuffers[outputBufferIndex]
            outputBuffer.position(mBufferInfo!!.offset)
            outputBuffer.limit(mBufferInfo!!.offset + outBitsSize)

            val outData = ByteArray(outPacketSize)
            addADTStoPacket(outData, outPacketSize)

            outputBuffer.get(outData, 7, outBitsSize)
            outputBuffer.position(mBufferInfo!!.offset)
            try {
                //接口回调方式
                if (encoderListener != null) {
                    encoderListener!!.encodedData(outData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mEncoder!!.releaseOutputBuffer(outputBufferIndex, false)
            outputBufferIndex = mEncoder!!.dequeueOutputBuffer(mBufferInfo!!, 0)
        }
    }

    /**
     * 初始化编码器
     */
    private fun initEncoder() {
        try {
            mEncoder = MediaCodec.createEncoderByType(MIMETYPE_AUDIO_AAC)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val format = MediaFormat.createAudioFormat(MIMETYPE_AUDIO_AAC,
                sampleRate, channelCount)
        format.setString(MediaFormat.KEY_MIME, MIMETYPE_AUDIO_AAC)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK,
                AudioFormat.CHANNEL_IN_MONO)//CHANNEL_IN_STEREO 立体声;
        mEncoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mEncoder!!.start()

        mBufferInfo = MediaCodec.BufferInfo()
    }

    fun setEncoderListener(encoderListener: EncoderListener) {
        this.encoderListener = encoderListener
    }

    interface EncoderListener {
        fun encodedData(data: ByteArray)
    }
    
    companion object {
        private const val MIMETYPE_AUDIO_AAC = "audio/mp4a-latm"
    }
}
