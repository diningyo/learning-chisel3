// See README.md for license details.
package sni.netsec.dma

import java.nio.file.Path

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import sni.util._

class SimPayloadTransactor(random: Boolean, dataBits: Int = 128)
                          (dataFileRootName: String, numOfData: Int = 65536)
  extends BaseSimTransactor(new Stream(dataBits), numOfData, random) {

  val m_dmem = Mem(numOfData, new Stream(dataBits))
  loadMemoryFromFile(m_dmem, dataFileRootName)

  w_finish := (m_dmem(0).data === r_ctr)
  io.finish := w_finish


  w_bits.data := Mux(w_finish, 0.U, m_dmem(r_ctr + 1.U).data)
  w_bits.strb := Mux(w_finish, 0.U, m_dmem(r_ctr).strb)
  w_bits.last := Mux(w_finish, false.B, m_dmem(r_ctr).last)
}

class OffsetTransactor(random: Boolean, dataBits: Int = 128)
                          (dataFileRootName: String, numOfData: Int = 65536)
  extends BaseSimTransactor(UInt(log2Ceil(dataBits / 8).W), numOfData, random) {

  val m_dmem = Mem(numOfData, UInt(32.W))
  loadMemoryFromFile(m_dmem, dataFileRootName)

  w_finish := (m_dmem(0) === r_ctr)
  io.finish := w_finish

  w_bits := Mux(w_finish, 0.U, m_dmem(r_ctr + 1.U))
}


/**
 * Queueモジュールのテストベンチ
 * @param random I/Fのタイミングをランダムにするかどうか
 * @param randSeed 乱数のシード
 * @param limit タイムアウトするサイクル数
 */
class SimDTMQueue
(
  dataBits: Int,
  random: (Boolean, Boolean) = (false, false),
  randSeed: BigInt = 1,
  limit: Int = 1000)(testDataFile: Map[String, Path], numOfTest: Int) extends BaseSimDTM(limit) {

  val strbBits = dataBits / 8

  val io = IO(new Bundle with BaseSimDTMIO {
    val random = Input(UInt(32.W))
    val mode = Input(Bool())
    val ofs = Input(UInt(strbBits.W))
  })

  val (inTiming, outTiming) = random

  // DUT
  val m_queue = Module(new align_buffer(dataBits))
  val m_data_xtor = Module(new SimPayloadTransactor(inTiming, dataBits)(testDataFile("input").toString))
  val m_ofs_xtor = Module(new OffsetTransactor(false, dataBits)(testDataFile("offset").toString))

  // PackedBuffer <-> SimTransactor
  m_queue.io.clk := clock
  m_queue.io.rst_n := !reset.asBool()
  m_queue.io.in <> m_data_xtor.io.data_in
  m_queue.io.ofs := m_ofs_xtor.io.data_in.bits

  m_data_xtor.io.start := true.B
  m_data_xtor.io.rand_seed := randSeed.U

  m_ofs_xtor.io.data_in.ready := m_data_xtor.io.data_in.fire() && m_data_xtor.io.data_in.bits.last
  m_ofs_xtor.io.start := true.B
  m_ofs_xtor.io.rand_seed := (randSeed + 0xfffffL).U

  // Output FIFO
  val m_ab_out_q = Module(
    new BaseResultQueue(chiselTypeOf(m_queue.io.out.bits), 100)(outTiming))
  m_ab_out_q.io.data_in <> m_queue.io.out
  m_ab_out_q.io.rand_seed := (randSeed + 0xfffffL).U

  val m_ab_out_comparator = Module(
    new SimComparator(dataBits)(testDataFile("output").toString))

  m_ab_out_comparator.io.data_in <> m_ab_out_q.io.data_out

  io.finish := m_data_xtor.io.finish && m_ab_out_comparator.io.finish
  io.result := m_ab_out_comparator.io.result
}
