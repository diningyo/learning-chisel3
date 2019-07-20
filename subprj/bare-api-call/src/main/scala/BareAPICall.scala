
import chisel3._
import chisel3.iotesters._

// 修正前：この状態ではエラーはなかった
class BeforeErrorMod extends Module {
  val io = IO(new Bundle {
    val out = Output(Vec(2, Bool()))
  })

  io.out(0) := true.B
  io.out(1) := false.B
}

// 修正後：これにしたらエラーが出た
class RegenerateErrorMod extends Module {
  val io = IO(new Bundle {
    val out = Output(UInt(2.W))
  })

  val out = Wire(Vec(2, Bool()))

  out(0) := true.B
  out(1) := false.B

  io.out := out.asUInt()
}

object TestElaborateBeforeErrorMod extends App {
  //
  iotesters.Driver.execute(args, () => new BeforeErrorMod()) {
    c => new PeekPokeTester(c) {
      expect(c.io.out(0), true)
      expect(c.io.out(1), true)
    }
  }
}

object TestElaborateRegenerateErrorModFail extends App {
  //
  iotesters.Driver.execute(args, () => new RegenerateErrorMod()) {
    c => new PeekPokeTester(c) {
      expect(c.io.out(0), true)
      expect(c.io.out(1), true)
    }
  }
}

object TestElaborateRegenerateErrorModOK extends App {
  //
  iotesters.Driver.execute(args, () => new RegenerateErrorMod()) {
    c => new PeekPokeTester(c) {

      def getBit(idx: Int): BigInt = (peek(c.io.out) >> idx) & 0x1

      expect(getBit(0).U, true)
      expect(getBit(1).U, false)
    }
  }
}
