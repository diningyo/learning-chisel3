import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._

object State extends ChiselEnum {
  val A = Value
  val B = Value
  val C = Value
}

/*
 *これはビルドエラーになる例
 */
/*
class NGPrameteriseSwitch(p: Boolean = true) extends Module {

  val io = IO(new Bundle {
    val out_stm = Output(UInt(State.getWidth.W))
  })

  val r_stm = RegInit(State.A)

  switch (r_stm) {
    is (State.A) {
      if (p) {
        r_stm := State.C
      } else {
        r_stm := State.B
      }
    }

    is (State.B) {
      r_stm := State.A
    }

    // switch の直下はis()以外はNGになる
    if (p) {
      is (State.C) {
        r_stm := State.A
      }
    }
  }

  io.out_stm := r_stm.asUInt()
}
*/

class PrameteriseSwitch(p: Boolean = true) extends Module {

object State extends ChiselEnum {
    val A = Value
    val B = Value
    val C = Value
  }

  val io = IO(new Bundle {
    val out_stm = Output(UInt(State.getWidth.W))
  })

  val r_stm = RegInit(State.A)
  switch (r_stm) {
    is (State.A) {
      if (p) {
        r_stm := State.C
      } else {
        r_stm := State.B
      }
    }

    is (State.B) {
      r_stm := State.A
    }
  }

  if (p) {
    switch (r_stm) {
      is (State.C) {
        r_stm := State.A
      }
    }
  }

  io.out_stm := r_stm.asUInt()
}

object Elaborate extends App {
  Driver.execute(Array("-td=rtl", "-tn=true"), () => new PrameteriseSwitch(true))
  Driver.execute(Array("-td=rtl", "-tn=false"), () => new PrameteriseSwitch(false))
  //Driver.execute(Array("-td=rtl", "-tn=false"), () => new PrameteriseSwitch(false))
}
