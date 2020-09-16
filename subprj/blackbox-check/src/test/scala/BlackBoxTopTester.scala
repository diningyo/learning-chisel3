
import chisel3._
import chisel3.iotesters._

class BlackBoxTopTester extends ChiselFlatSpec {
  behavior of "BlackboxTop"

  it should "be able to elaborate" in {
    iotesters.Driver.execute(Array(
      "-tn=BlackboxTop",
      "-td=test_run_dir/BlackboxTop",
      "-tbn=verilator"
    ),
      () => new BlackBoxTop
    ) {
      c => new PeekPokeTester(c) {

      }
    } should be (true)
  }
}
