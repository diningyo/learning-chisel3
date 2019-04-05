
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

/**
  * UInt/SIntに右ビットシフトを適用した場合の挙動を確認するモジュール
  */
class ShiftTestModule extends Module {
  val io = IO(new Bundle {
    val uintOut = Output(UInt(32.W))
    val sintOut = Output(SInt(32.W))
    val sintToUIntOut = Output(UInt(32.W))
  })

  io.uintOut := BigInt("deadbeaf", 16).asUInt() >> 8.U // UIntの値を右シフト
  io.sintOut := (0xdeadbeaf.S >> 8.U)                  // SIntの値を右シフト
  io.sintToUIntOut := (0xdeadbeaf.S >> 8.U).asUInt()   // SIntの値を右シフトした後にUIntに変換
}

class UIntAndSIntShiftTester extends ChiselFlatSpec {
  behavior of "ShiftTest"

  it should "UIntの右シフトは論理シフトになる" in {
    Driver.execute(Array(""), () => new ShiftTestModule) {
      c =>
        new PeekPokeTester(c) {
          println(s"0x${peek(c.io.uintOut).toInt.toHexString}")
          expect(c.io.uintOut, 0xdeadbe)
        }
    }
  }

  it should "SIntの右シフトは算術シフトとなり、空いた上位ビットは符号ビット拡張が行われる" in {
    Driver.execute(Array(""), () => new ShiftTestModule) {
      c =>
        new PeekPokeTester(c) {
          println(s"0x${peek(c.io.sintOut).toInt.toHexString}")
          expect(c.io.sintOut, 0xffdeadbe)
        }
    }
  }

  it should "SIntを右にシフトしたものをUIntに変換すると、符号ビットが0にセットされる" in {
    Driver.execute(Array(""), () => new ShiftTestModule) {
      c =>
        new PeekPokeTester(c) {
          println(s"0x${peek(c.io.sintToUIntOut).toInt.toHexString}")
          expect(c.io.sintToUIntOut, 0xfdeadbe)
        }
    }
  }

}

