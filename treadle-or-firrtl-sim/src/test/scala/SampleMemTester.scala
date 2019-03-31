
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class SampleMemUnitTester(c: SampleMem) extends PeekPokeTester(c) {
  def write(addr: BigInt, wrData: BigInt): Unit = {
    poke(c.io.addr, addr)
    poke(c.io.wren, true)
    poke(c.io.wrData, wrData)

    step(1)
  }

  def read(addr: BigInt): BigInt = {
    poke(c.io.addr, addr)
    poke(c.io.rden, true)
    peek(c.io.rdData)
  }
}




class SampleMemTester extends ChiselFlatSpec{
  behavior of "Chiselのテスト"

  val defaultMemSize= 1024

  val defaultArgs = Array(
    "--generate-vcd-output", "on"
  )

  it should "同じサイクルに複数回端子設定をすると結果が揺れる？？" in {
    Driver.execute(defaultArgs ++ Array("--backend-name", "firrtl"), () => new SampleMem(defaultMemSize)) {
    //Driver.execute(defaultArgs ++ Array("--backend-name", "verilator"), () => new SampleMem(defaultMemSize)) {
    //Driver.execute(defaultArgs ++ Array("--backend-name", "verilator"), () => new SampleMem(defaultMemSize)) {
          c => new SampleMemUnitTester(c) {
        for (i <- 0 until 10) {
          poke(c.io.wren, true)
          poke(c.io.addr, 0)
          poke(c.io.wrData, 0xff)
          poke(c.io.wren, false)
          poke(c.io.addr, 1)
          poke(c.io.wrData, 0xab)
          step(1)
        }
      }
    } should be (true)
  }
}
