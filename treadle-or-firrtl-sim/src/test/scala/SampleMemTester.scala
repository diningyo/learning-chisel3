// See LICENSE for license details.
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class SimpleMemUnitTester(c: SimpleMem) extends PeekPokeTester(c) {
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

class MemTester extends ChiselFlatSpec{
  behavior of "ChiselのPeekPokeTester"

  val defaultMemSize= 1024

  val defaultArgs = Array(
    "--generate-vcd-output", "on"
  )

  it should "同じサイクルに複数回端子設定をすると結果が揺れる - test1" in {
    //val backendList = Array("firrtl", "treadle", "verilator")
    val backendList = Array("firrtl", "treadle")

    for (backend <- backendList) {
      val targetDir = "test_run_dir/test1"
      val topName = backend
      val optionArgs = Array("--target-dir", targetDir, "--top-name", topName)
      Driver.execute(defaultArgs ++ optionArgs, () => new SimpleMem(defaultMemSize)) {
        c =>
          new SimpleMemUnitTester(c) {
            for (i <- 0 until 10) {
              // とりあえず露骨に同じサイクルで設定してみる
              poke(c.io.wren, true)
              poke(c.io.addr, 0)
              poke(c.io.wrData, 0xff)
              poke(c.io.wren, false)
              poke(c.io.addr, 1)
              poke(c.io.wrData, 0xab)
              step(1)
            }
          }
      } should be(true)
    }
  }
}
