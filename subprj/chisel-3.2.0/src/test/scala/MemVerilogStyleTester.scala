
import chisel3.iotesters._


class MemVerilogStyleTester extends ChiselFlatSpec {

  it should "" in {
    Driver.execute(
      Array(
        "-tbn=verilator"
      ), () => new MemVerilogStyle) {
      c => new PeekPokeTester(c) {
        reset()
        poke(c.io.rden, true)
        for (addr <- 0 until 16) {
          poke(c.io.addr, addr)
          println(f"c.io.rddata = 0x${peek(c.io.rddata)}%08x")
          step(1)
        }
        step(1)
      }
    }
  }
}
