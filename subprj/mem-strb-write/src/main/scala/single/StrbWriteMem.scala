
package single

import chisel3._
import chisel3.util._

import scala.language.reflectiveCalls

class StrbWriteMem(memWordNum: Int, dataBits: Int) extends Module {

  val io = IO(new Bundle {
    val addr = Input(UInt(log2Ceil(memWordNum).W))
    val wren = Input(Bool())
    val wrstrb = Input(UInt((dataBits / 8).W))
    val wrdata = Input(UInt(dataBits.W))
    val rddata = Output(UInt(dataBits.W))
  })

  //val mem = Mem(memWordNum, UInt(dataBits.W))
  val mem = Mem(memWordNum, Vec(4, UInt(8.W)))

  val wrdataVec = VecInit(Range(0, dataBits, 8).map(i => io.wrdata(i + 7, i)))

  when (io.wren) {
    // 作ったメモリによって呼べるライトが変わるっぽい。
    // <:< で型を限定できるらしい。。
    // - https://qiita.com/mtoyoshi/items/fbf2c744e3c7fd69a438
    // - https://qiita.com/mtoyoshi/items/13ac0500aa23fb4fa0d4
    //mem.write(io.addr, VecInit(Seq.fill(4)(0xff.U)), VecInit(Seq(true, false, true, false)))
    mem.write(io.addr, wrdataVec, io.wrstrb.toBools)
  }

  val rddata = Cat(RegNext(mem(io.addr)))

  io.rddata := rddata
}

object Elaborate extends App {
  Driver.execute(Array(
    "-td=rtl/single/strb-write-mem"
  ),
    () => new StrbWriteMem(65536, 32)
  )
}