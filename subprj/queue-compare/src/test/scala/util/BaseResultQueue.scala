
package sni.util

import chisel3._
import chisel3.experimental.{DataMirror, requireIsChiselType}
import chisel3.util._

class BaseResultQueue[T <: Data](gen: T, memSize: Int = 1)(timing: Boolean) extends Module {

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
    val data_in = Flipped(DecoupledIO(genType))
    val data_out = DecoupledIO(genType)
    val count = Output(UInt(log2Ceil(memSize + 1).W))
  })

  val m_xor_shift = Module(new XorShift32(0, 65536))
  val m_result_q = Module(new Queue(chiselTypeOf(io.data_in.bits), memSize, pipe = true, flow = true))
  io.data_out <> m_result_q.io.deq
  io.count := m_result_q.io.count
  m_result_q.io.enq <> io.data_in

  if (timing) {
    val w_rand_out = m_xor_shift.io.rand_out
    val w_rand = MuxCase(w_rand_out(0), Seq(
      w_rand_out(15) -> (w_rand_out(8, 0) === 0.U),
      w_rand_out(14) -> (w_rand_out(4, 0) === 0.U),
      w_rand_out(13) -> (w_rand_out(2, 0) === 0.U),
      w_rand_out(12) -> (w_rand_out(1, 0) === 0.U)
    ))

    m_result_q.io.enq.valid := Mux(io.data_in.valid, w_rand, false.B)
    io.data_in.ready := Mux(m_result_q.io.enq.ready, w_rand, false.B)
  }

  m_xor_shift.io.seed := io.rand_seed
  m_xor_shift.io.update := true.B
}
