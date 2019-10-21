
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._

/**
 * ChiselEnumの確認コード
 */
class TrialStrongEnums extends Module {
  val io = IO(new Bundle {
    val wren = Input(Bool())
    val rden = Input(Bool())
    val rddata = Output(UInt(8.W))
  })

  /**
   * これがStrong Enumsと呼ばれているChiselEnum
   */
  object State extends ChiselEnum {
    val sIdle, sWrite = Value
    val sRead = Value(3.U)
  }

  val r_state = RegInit(State.sIdle)

  switch (r_state) {
    is (State.sIdle) {
      when (io.wren) {
        r_state := State.sWrite
      } .elsewhen(io.rden) {
        r_state := State.sRead
      }
    }

    is (State.sWrite) {
      r_state := State.sIdle
    }

    is (State.sRead) {
      r_state := State.sIdle
    }
  }

  io.rddata := Mux(r_state === State.sRead, 100.U, 0.U)
}

/**
 * RTLの生成
 */
object ElaborateStrongEnums extends App {
  Driver.execute(
    Array(
      "-td=rtl/chisel-3.2.0/TrialStrongEnums"
    ), () => new TrialStrongEnums)
}