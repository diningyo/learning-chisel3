// See README.md for license details.
package sni.netsec.dma

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

class SimComparator(dataBits: Int)(dataFileRootName: String, numOfData: Int = 65536) extends Module {
  val io = IO(new Bundle {
    val data_in = Flipped(new IntStreamIO(dataBits))
    val finish = Output(Bool())
    val result = Output(Bool())
  })

  val m_dmem = Mem(numOfData, new IntStreamIO(dataBits).bits)
  loadMemoryFromFile(m_dmem, dataFileRootName)

  val w_test_data_count = m_dmem(0).data
  val r_data_count = RegInit(0.U(32.W))
  val r_result_strb = RegInit(false.B)
  val r_result_data = RegInit(false.B)
  val r_result_last = RegInit(false.B)
  val r_result_error = RegInit(false.B)

  val w_exp_strb = m_dmem(r_data_count).strb
  val w_exp_data = m_dmem(r_data_count + 1.U).data
  val w_exp_last = m_dmem(r_data_count).last

  when (io.data_in.fire()) {
    r_data_count := r_data_count + 1.U

    when (w_exp_strb =/= io.data_in.bits.strb) {
      r_result_strb := true.B
      printf("NG!! : strb (raw, exp) = (0x%x, 0x%x)\n", io.data_in.bits.strb, w_exp_strb)
    }

    val w_fail_flags = WireDefault(VecInit(Seq.fill(w_exp_strb.getWidth)(false.B)))
    val w_raw_bytes_data = VecInit(Range(dataBits, 0, -8).map(i => io.data_in.bits.data(i - 1, i - 8)))
    val w_exp_bytes_data = VecInit(Range(dataBits, 0, -8).map(i => w_exp_data(i - 1, i - 8)))

    w_raw_bytes_data.zip(w_exp_bytes_data).zip(
      w_exp_strb.asBools().reverse).zip(w_fail_flags) .foreach {
      case (((raw, exp), strb), fail) =>
        when (strb && (raw =/= exp)) {
          printf("NG!! : data (strb, raw, exp) = (%d, 0x%x, 0x%x)\n", strb, raw, exp)
          fail := true.B
        }
    }

    when (w_fail_flags.reduce(_ || _)) {
      r_result_data := true.B
      printf("NG!! : data(i)(raw, exp) = (%d)(0x%x, 0x%x)\n", r_data_count, io.data_in.bits.data, w_exp_data)
    }

    when (w_exp_last =/= io.data_in.bits.last) {
      r_result_last := true.B
      printf("NG!! : last (raw, exp) = (%d, %d)\n", io.data_in.bits.last, w_exp_last)
    }
  }

  io.data_in.ready := !reset.asBool()
  io.finish := r_data_count === w_test_data_count
  io.result := r_result_strb || r_result_data || r_result_last || r_result_error
}
