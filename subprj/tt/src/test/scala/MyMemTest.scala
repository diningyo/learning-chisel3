
import chisel3.iotesters._

class MyMemTest extends ChiselFlatSpec {
  behavior of "MyMem"

  it should "指定したアドレスにデータがライト出来る" in {
    Driver.execute(Array(""),
      () => new MyMem(256, 32)) {
      c => new MyMemTester(c) {
        write(0x10, 0x12345678, 0xf)
        idle(1)

        // リード
        read(0x10, 0x12345678)
      }
    } should be (true)
  }

  it should "指定したアドレスからデータがリード出来る" in {
    Driver.execute(Array(""),
      () => new MyMem(256, 32)) {
      c => new MyMemTester(c) {
        write(0x10, 0x12345678, 0xf)
        idle(1)

        // リード
        read(0x10, 0x12345678)
      }
    } should be (true)
  }

  it should "指定したアドレスにストローブを使ったデータ・ライト出来る" in {
    Driver.execute(Array(""),
      () => new MyMem(256, 32)) {
      c => new MyMemTester(c) {
        val wrdata = 0x12345678
        for (strb <- 0 to 16) {
          val addr = strb
          write(addr, wrdata, strb)
        }

        idle(1)

        for (strb <- 0 to 16) {
          val addr = strb
          val mask = Range(0, 4).map(i => {
            val a = if (((strb >> i) & 0x1) == 0x1) 0xff else 0x0
            a << (i * 8)
          }).reduce(_|_)
          val exp = wrdata & mask
          read(addr, exp)
        }
      }
    } should be (true)
  }
}
