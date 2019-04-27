// See LICENSE for license details.

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class BundleRegInitTester extends ChiselFlatSpec {

  behavior of "SampleBundleRegInit0"

  val defaultArgs = Array(
    "--backend-name=verilator"
  )

  it should "初期化時にWireに設定した値がレジスタに設定される" in {
    Driver.execute(defaultArgs, () => new SampleBundleRegInit0) {

      c => new PeekPokeTester(c) {
        reset()

        // リセット後の初期値を確認
        expect(c.io.out.a, 1)
        expect(c.io.out.b, 2)

        // ライトして値が変わることを確認
        poke(c.io.en, true)
        poke(c.io.data, 0xff)
        step(1)
        expect(c.io.out.a, 0xf)
        expect(c.io.out.b, 0xf)
      }
    } should be (true)
  }

  behavior of "SampleBundleRegInit1"

  it should "初期化時にinit()で設定した値がレジスタに設定される" in {
    Driver.execute(defaultArgs, () => new SampleBundleRegInit1) {

      c => new PeekPokeTester(c) {
        reset()

        // リセット後の初期値を確認
        expect(c.io.out.a, 2)
        expect(c.io.out.b, 3)

        // ライトして値が変わることを確認
        poke(c.io.en, true)
        poke(c.io.data, 0xff)
        step(1)
        expect(c.io.out.a, 0xf)
        expect(c.io.out.b, 0xf)
      }
    } should be (true)
  }

  behavior of "SampleBundleRegInit2"

  it should "初期化時にapply()で設定した値がレジスタに設定される" in {
    Driver.execute(defaultArgs, () => new SampleBundleRegInit2) {

      c => new PeekPokeTester(c) {
        reset()

        // リセット後の初期値を確認
        expect(c.io.out1.a, 3)
        expect(c.io.out1.b, 4)
        expect(c.io.out2.a, 0x7)
        expect(c.io.out2.b, 0x7)

        // ライトして値が変わることを確認
        poke(c.io.en, true)
        poke(c.io.data, 0xff)
        step(1)
        expect(c.io.out1.a, 0xf)
        expect(c.io.out1.b, 0xf)
        expect(c.io.out2.a, 0xf)
        expect(c.io.out2.b, 0xf)
      }
    } should be (true)
  }
}
