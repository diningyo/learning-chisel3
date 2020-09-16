
import chisel3.iotesters._


class TrialHasBlackBoxPathTester extends ChiselFlatSpec {

  it should "addPathで指定したパスのRTLが読み込める" in {
    Driver.execute(
      Array(
        "-tbn=verilator",
        "-td=test_run_dir/chisel-3.2.0/TrialHasBlackBoxPath"
      ), () => new TrialHasBlackBoxPath) {
      c => new PeekPokeTester(c) {
        reset()
        poke(c.io.a, true)
        poke(c.io.b, true)
        expect(c.io.aa, true)
        expect(c.io.bb, true)
      }
    }
  }
}
