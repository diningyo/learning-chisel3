// See LICENSE for license details.

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

/**
  * シンプルなメモリ
  * @param loadFilePath 読み込むHEX文字列データが入ったファイルのパス
  * @param fileType ファイルタイプ
  */
class Memory(val loadFilePath: String,
             val fileType: MemoryLoadFileType.FileType = MemoryLoadFileType.Hex) extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(10.W))
    val wren = Input(Bool())
    val rden = Input(Bool())
    val wrData = Input(UInt(8.W))
    val rdData = Output(UInt(8.W))
  })

  val m = Mem(16, UInt(8.W))

  /**
    * これでファイル中のメモリデータがメモリにロードされる
    * loadFilePathが存在してる時にのみ、読むようにした方が良かったな、コレ。
    */
  if (loadFilePath != "") {
    println(s"load file : $loadFilePath")
    loadMemoryFromFile(m, loadFilePath, fileType)
  }

  when(io.wren) {
    m(io.addr) := io.wrData
  }

  io.rdData := m(io.addr)
}
