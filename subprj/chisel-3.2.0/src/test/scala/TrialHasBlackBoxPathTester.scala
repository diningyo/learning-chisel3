
import chisel3.iotesters._


class TrialHasBlackBoxPathTester extends ChiselFlatSpec {

  it should "" in {
    Driver.execute(
      Array(
        "-tbn=verilator"
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
