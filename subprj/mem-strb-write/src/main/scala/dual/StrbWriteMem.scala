
package dual

import scala.language.reflectiveCalls
import chisel3._
import chisel3.util._

class StrbWriteMem(memWordNum: Int, dataBits: Int) extends Module {

  val io = IO(new Bundle {
    val addra = Input(UInt(log2Ceil(memWordNum).W))
    val wrena = Input(Bool())
    val wrstrba = Input(UInt((dataBits / 8).W))
    val wrdataa = Input(UInt(dataBits.W))
    val rddataa = Output(UInt(dataBits.W))
    val clockb = Input(Clock())
    val addrb = Input(UInt(log2Ceil(memWordNum).W))
    val wrenb = Input(Bool())
    val wrstrbb = Input(UInt((dataBits / 8).W))
    val wrdatab = Input(UInt(dataBits.W))
    val rddatab = Output(UInt(dataBits.W))
  })

  //val mem = Mem(memWordNum, UInt(dataBits.W))
  val mem = Mem(memWordNum, Vec(4, UInt(8.W)))
  val memb = mem
  val wrdataVecA = VecInit(Range(0, dataBits, 8).map(i => io.wrdataa(i + 7, i)))
  val wrdataVecB = VecInit(Range(0, dataBits, 8).map(i => io.wrdatab(i + 7, i)))

  when (io.wrena) {
    // 作ったメモリによって呼べるライトが変わるっぽい。
    // <:< で型を限定できるらしい。。
    // - https://qiita.com/mtoyoshi/items/fbf2c744e3c7fd69a438
    // - https://qiita.com/mtoyoshi/items/13ac0500aa23fb4fa0d4
    //mem.write(io.addr, VecInit(Seq.fill(4)(0xff.U)), VecInit(Seq(true, false, true, false)))
    mem.write(io.addra, wrdataVecA, io.wrstrba.toBools)
  }
  val rddataa = Cat(RegNext(mem(io.addra)))
  io.rddataa := rddataa


  withClock(io.clockb) {
    when (io.wrenb) {
      memb.write(io.addrb, wrdataVecB, io.wrstrbb.toBools)
    }

    val rddatab = Cat(RegNext(memb(io.addrb)))

    io.rddatab := rddatab
  }
}

object Elaborate extends App {
  Driver.execute(Array(
    "-td=rtl/dual/strb-write-mem"
  ),
    () => new StrbWriteMem(65536, 32)
  )
}
