// See LICENSE for license details.

import chisel3._
import chisel3.util.Cat

class Mem2D(useWriteTask: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val wren = Input(Bool())
    val rden = Input(Bool())
    val addr = Input(UInt(14.W))
    val wrdata = Input(UInt(32.W))
    val rddata = Output(UInt(32.W))
  })

  val m = Mem(16, Vec(4, UInt(8.W)))

  if (useWriteTask) {
    val wrdata = Wire(Vec(4, UInt(8.W)))
    for (i <- 0 until m(0).length) {
      wrdata(i) := io.wrdata(((i + 1) * 8) - 1, i * 8)
    }
    m.write(io.addr, wrdata, Seq.fill(4)(true.B))
  } else {
    val mask = Seq.fill(4)(true.B)
    for (i <- 0 until m(0).length) {
      when(io.wren && mask(i)) {
        m(io.addr)(i) := io.wrdata(((i + 1) * 8) - 1, i * 8)
      }
    }
  }

  io.rddata := Cat(m.read(io.addr).reverse)
}

/**
  * RTL生成用メインオブジェクト
  */
object ElaborateMem2D extends App {
  val dstDir = "rtl/mem2d"
  Driver.execute(Array[String]("-tn=Mem2D", s"-td=$dstDir"), () => new Mem2D())
  Driver.execute(Array[String]("-tn=Mem2DWithWrite", s"-td=$dstDir"), () => new Mem2D(true))
}