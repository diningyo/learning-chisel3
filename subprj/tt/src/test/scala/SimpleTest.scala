
import chisel3.iotesters.{Driver, PeekPokeTester}

class MyMemTester(c: MyMem) extends PeekPokeTester(c) {

  def idle(cycle: Int): Unit = {
    poke(c.io.wren, false)
    step(cycle)
  }

  def write(addr: BigInt, data: BigInt, strb: BigInt): Unit = {
    // ライトに関連した端子に値を設定
    poke(c.io.addr, addr)
    poke(c.io.wren, true)
    poke(c.io.wrstrb, strb)
    poke(c.io.wrdata, data)
    // stepで1cycle進める
    step(1)
  }

  def read(addr: BigInt, exp: BigInt): BigInt = {
    // リード
    poke(c.io.addr, addr)

    // 1cycle後にリードデータが出てくるので1cycle進める
    step(1)
    expect(c.io.rddata, exp)
  }
}

object Test extends App {
  Driver.execute(args, () => new MyMem(256, 32)) {
    c => new PeekPokeTester(c) {
      reset(10)
      // ライトに関連した端子に値を設定
      poke(c.io.addr, 0x10)
      poke(c.io.wren, true)
      poke(c.io.wrstrb, 0xf)
      poke(c.io.wrdata, 0x12345678)

      // stepで1cycle進める
      step(1)

      // ライトの制御端子をfalseに設定
      poke(c.io.wren, false)

      // リード
      poke(c.io.addr, 0x10)

      // 1cycle後にリードデータが出てくるので1cycle進める
      step(1)
      expect(c.io.rddata, 0x12345678)
      println(f"c.io.rddata = 0x${peek(c.io.rddata)}%08x")
    }
  }
}

object Test2 extends App {
  Driver.execute(args, () => new MyMem(256, 32)) {
    c => new MyMemTester(c) {
      reset(10)
      // ライトに関連した端子に値を設定
      write(0x10, 0x12345678, 0xf)
      idle(1)

      // リード
      read(0x10, 0x12345678)
    }
  }
}