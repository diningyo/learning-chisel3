// See LICENSE for license details.

import chisel3._
import chisel3.util._

/**
  * 3次元のメモリ
  * @param useWriteTask MemBaseのwriteメソッドを使うかどうかを選択
  */
class Mem3D(useWriteTask: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val wren = Input(Vec(2, Bool()))
    val rden = Input(Bool())
    val addr = Input(UInt(14.W))
    val wrdata = Input(Vec(2, UInt(32.W)))
    val rddata = Output(Vec(2, UInt(32.W)))
  })

  val m = Mem(16, Vec(2, Vec(4, UInt(8.W))))

  if (useWriteTask) {
    val wrdata = Wire(Vec(2, Vec(4, UInt(8.W))))

    for {i <- 0 until m(0).length
         j <- 0 until m(0)(0).length} {
      wrdata(i)(j) := io.wrdata(i)(((j + 1) * 8) - 1, j * 8)
    }

    m.write(io.addr, wrdata, Seq.fill(2)(true.B))
  } else {
    for {i <- 0 until m(0).length
         j <- 0 until m(0)(0).length} {
      when (io.wren(i)) {
        m(io.addr)(i)(j) := io.wrdata(i)(((j + 1) * 8) - 1, j * 8)
      }
    }
  }

  for (i <- 0 until m(0).length) {
    io.rddata(i) := Cat(m(io.addr)(i).reverse)
  }
}

/**
  * RTL生成用メインオブジェクト
  */
object ElaborateMem3D extends App {
  val dstDir = "rtl/mem3d"
  Driver.execute(Array[String]("-tn=Mem3D", s"-td=$dstDir"), () => new Mem3D())
  Driver.execute(Array[String]("-tn=Mem3DWithWrite", s"-td=$dstDir"), () => new Mem3D(true))
}