
import chisel3._
import chisel3.iotesters.PeekPokeTester

class SupportTAB extends Module {
  val io = IO(new Bundle {})

  printf("io.in\t%x\n", 0xff.U)
}

object TestSupportTAB extends App {
  iotesters.Driver.execute(args, () => new SupportTAB) {
    c => new PeekPokeTester(c) {
      step(2)
    }
  }
}