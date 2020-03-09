// See README.md for license details.


import chisel3.iotesters._

class ObjTestVecTester extends ChiselFlatSpec {

  val args = Array("-tbn=treadle")

  it should "Vecの入力端子をインデックス指定して渡すのは無理" in {
    Driver.execute(args, () => new ObjTestVec) {
      c => new PeekPokeTester(c) {
        poke(c.io.in(0).b, true)
        poke(c.io.in(0).vec_uint(0), 8)
        expect(c.io.in(0).b, true)
        expect(c.io.in(0).vec_uint(0), 8)
      }
    } should be (true)
  }


  it should "Vecの入力端子にはSeqが渡せる" in {
    Driver.execute(args, () => new ObjTestVec) {
      c => new PeekPokeTester(c) {

      }
    } should be (true)
  }

  it should "ループで単体を取り出して渡すこともできる" in {
    Driver.execute(args, () => new ObjTestVec) {
      c => new PeekPokeTester(c) {

      }
    } should be (true)
  }
}
