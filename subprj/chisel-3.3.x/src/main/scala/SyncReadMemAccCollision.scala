// See README.md for license details.

import chisel3._
import chisel3.iotesters._
import chisel3.stage.ChiselStage

class SyncReadMemAccCollision(ruw: SyncReadMem.ReadUnderWrite)
  extends Module {
  val io = IO(new Bundle {
    val wren = Input(Bool())
    val wraddr = Input(UInt(4.W))
    val wrdata = Input(UInt(8.W))
    val rdaddr = Input(UInt(4.W))
    val rden = Input(Bool())
    val rddata = Output(UInt(8.W))
  })

  val m_sync_read_mem = SyncReadMem(10, UInt(8.W), ruw)

  when (io.wren) {
    m_sync_read_mem.write(io.wraddr, io.wrdata)
  }

  io.rddata := m_sync_read_mem.read(io.rdaddr, io.rden)
}

object ElaborateSRMA extends App {

  val ruwList = List(
    SyncReadMem.ReadFirst,
    SyncReadMem.WriteFirst,
    SyncReadMem.Undefined)

  ruwList.foreach {
    ruw =>
      (new ChiselStage).emitVerilog(
        new SyncReadMemAccCollision(ruw),
        Array("-td=rtl", s"-o=SyncReadMemAccCollision_${ruw.toString}")
      )
  }
}

object TestSRMA extends App {

  val ruwList = List(
    SyncReadMem.ReadFirst,
    SyncReadMem.WriteFirst,
    SyncReadMem.Undefined)

  ruwList.foreach {
    ruw =>
      val dutName = s"SyncReadMemAccCollision_${ruw.toString}"

      iotesters.Driver.execute(
        Array(
          s"-td=test_run_dir/$dutName",
          s"-tn=$dutName",
          "-tgvo=on"
        ),
        () => new SyncReadMemAccCollision(ruw)) {
        c => new PeekPokeTester(c) {
          // データ書き込み
          poke(c.io.wraddr, 0)
          poke(c.io.wren, true)
          poke(c.io.wrdata, 0xff)
          step(1)

          // リード・ライトアクセス競合
          poke(c.io.wraddr, 0)
          poke(c.io.wren, true)
          poke(c.io.wrdata, 0x80)
          poke(c.io.rdaddr, 0)
          poke(c.io.rden, true)
          poke(c.io.rddata, 0x80)
          step(2)
        }
      }
  }
}