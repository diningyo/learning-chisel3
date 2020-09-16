// See README.md for license details.

package sni.util

import chisel3._
import chisel3.experimental.{DataMirror, requireIsChiselType}
import chisel3.util._

/**
 * BaseSimTransactor
 * @param gen
 * @param numOfTest
 * @param random
 * @tparam T Data
 */
class BaseSimTransactor[T <: Data](
                    gen: T,
                    numOfTest: Int,
                    random: Boolean) extends Module {

  val genType = if (compileOptions.declaredTypeMustBeUnbound) {
    requireIsChiselType(gen)
    gen
  } else {
    if (DataMirror.internal.isSynthesizable(gen)) {
      chiselTypeOf(gen)
    } else {
      gen
    }
  }

  val io = IO(new Bundle {
    val rand_seed = Input(UInt(32.W))
    val start = Input(Bool())
    val data_in = DecoupledIO(genType)
    val finish = Output(Bool())
  })

  val m_xor_shift = Module(new XorShift32(0, 1023))
  val m_valid_q = Module(new Queue(genType, 1, true, true))
  val w_bits = Wire(genType)

  val ctrBits = log2Ceil(numOfTest + 1)
  val r_ctr = RegInit(0.U(ctrBits.W))

  val w_finish = Wire(Bool())
  w_finish := r_ctr === numOfTest.U

  when (!w_finish && m_valid_q.io.enq.fire()) {
    r_ctr := r_ctr + 1.U
  }

  m_xor_shift.io.seed := io.rand_seed
  m_xor_shift.io.update := true.B

  // IO
  val w_valid = WireDefault(!w_finish)

  if (random) {
    val w_rand_out = m_xor_shift.io.rand_out
    val w_rand = MuxCase(w_rand_out(0), Seq(
      w_rand_out(9) -> (w_rand_out(1, 0) === 0.U)
    ))

    w_valid := !w_finish && w_rand
  }


  m_valid_q.io.enq.valid := io.start && w_valid
  m_valid_q.io.enq.bits := w_bits
  m_valid_q.io.deq.ready := io.data_in.ready

  io.data_in <> m_valid_q.io.deq
  io.data_in.valid := io.start && m_valid_q.io.deq.valid
  io.finish := w_finish
}
