
import chisel3.iotesters._

class MyBundleTester extends ChiselFlatSpec {

  behavior of "MyModule"

  it should "Bundleを使って作ったやつの副作用" in {
    Driver.execute(Array(
      "--generate-vcd-output=on"
    ), () => new MyModule) {
      c => new PeekPokeTester(c) {
        poke(c.io.in, 0xff01)

        // レジスタなので1サイクル後に変化する（と思ってた)
        step(1)
        expect(c.io.out.a, 0xff)
        expect(c.io.out.b, 0x1)
        //expect(c.io.out.valid, false)
      }
    } should be (true)
  }
}
