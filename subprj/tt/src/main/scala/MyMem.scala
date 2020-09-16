
import chisel3._
import chisel3.util._

class MyMem (numOfWords: Int, dataBits: Int) extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(log2Ceil(numOfWords).W))
    val wren = Input(Bool())
    val wrstrb = Input(UInt((dataBits / 8).W))
    val wrdata = Input(UInt(dataBits.W))
    val rddata = Output(UInt(dataBits.W))
  })
  // メモリはUInt(8.W) x Nで1wordのメモリ
  val m = Mem(256, Vec(io.wrstrb.getWidth, UInt(8.W)))

  // 32bitを8bit x NのVecに変換
  val wrdata = Wire(Vec(io.wrstrb.getWidth, UInt(8.W)))

  // 8bit毎にVecに格納
  for (i <- 0 until wrdata.length) {
    wrdata(wrdata.length - i - 1) := io.wrdata((i * 8) + 7, i * 8)
  }

  when (io.wren) {
    m.write(io.addr, wrdata, io.wrstrb.toBools.reverse)
  }

  // rddataはMemがVec(4, UInt(8.W))なので
  // その形で返却される
  val rddata = m.read(io.addr)
  io.rddata := RegNext(Cat(rddata))
}