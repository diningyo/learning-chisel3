// See LICENSE for license details.
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class BundleTestModule1Tester extends ChiselFlatSpec {
  val topName = "BundleTestModule1"

  behavior of topName

  val defaultArgs = Array("--generate-vcd-output=on")

  it should "a.aの値の変化に合わせてa.aaの値が変化する" in {
    val targetDir = s"test_run_dir/$topName/atest"

    val args = defaultArgs ++ Array(
      s"--top-name=$topName",
      s"--target-dir=$targetDir"
    )

    Driver.execute(args, () => new BundleTestModule1(false)) {
      c => new PeekPokeTester(c) {
        reset()

        expect(c.io.a.aa, 0x87654321L.U)
        poke(c.io.a.a, true)
        step(1)
        expect(c.io.a.aa, 0x12345678.U)
        step(1)
      }
    } should be (true)
  }

  it should "b.bの値の変化に合わせてb.bbの値が変化する" in {
    val targetDir = s"test_run_dir/$topName/btest"
    val args = defaultArgs ++ Array(
      s"--top-name=$topName",
      s"--target-dir=$targetDir"
    )

    Driver.execute(args, () => new BundleTestModule1(true)) {
      c => new PeekPokeTester(c) {
        reset()

        val b = c.io.b.get

        expect(b.bb, 0x87654321L.U)
        poke(b.b, true)
        step(1)
        expect(b.bb, 0x12345678.U)
        step(1)
      }
    } should be (true)
  }
}

class BundleTestModule2Tester extends ChiselFlatSpec {
  val topName = "BundleTestModule2"

  behavior of topName

  val defaultArgs = Array(
    "--generate-vcd-output=on"
    //"--backend-name=verilator"
  )

  it should "a.aの値の変化に合わせてa.aaの値が変化する" in {
    val targetDir = s"test_run_dir/$topName/atest"

    val args = defaultArgs ++ Array(
      s"--top-name=$topName",
      s"--target-dir=$targetDir"
    )

    Driver.execute(args, () => new BundleTestModule2(false)) {
      c => new PeekPokeTester(c) {
        reset()

        expect(c.io.a.aa, 0x87654321L.U)
        poke(c.io.a.a, true)
        step(1)
        expect(c.io.a.aa, 0x12345678.U)
        step(1)
      }
    } should be (true)
  }

  it should "b.bの値の変化に合わせてb.bbの値が変化する" in {
    val targetDir = s"test_run_dir/$topName/btest"
    val args = defaultArgs ++ Array(
      s"--top-name=$topName",
      s"--target-dir=$targetDir"
    )

    Driver.execute(args, () => new BundleTestModule2(true)) {
      c => new PeekPokeTester(c) {
        reset()

        val b = c.io.b.get

        expect(b.bb, 0x87654321L.U)
        poke(b.b, true)
        step(1)
        expect(b.bb, 0x12345678.U)
        step(1)
      }
    } should be (true)
  }
}

class BundleTestModule3Tester extends ChiselFlatSpec {
  val topName = "BundleTestModule3"

  behavior of topName

  val defaultArgs = Array(
    "--generate-vcd-output=on"
    //"--backend-name=verilator"
  )

  it should "a.aの値の変化に合わせてa.aaの値が変化する" in {
    val targetDir = s"test_run_dir/$topName/atest"

    val args = defaultArgs ++ Array(
      s"--top-name=$topName",
      s"--target-dir=$targetDir"
    )

    Driver.execute(args, () => new BundleTestModule3(false)) {
      c => new PeekPokeTester(c) {
        reset()

        expect(c.io.a.aa, 0x87654321L.U)
        poke(c.io.a.a, true)
        step(1)
        expect(c.io.a.aa, 0x12345678.U)
        step(1)
      }
    } should be (true)
  }

  it should "b.bの値の変化に合わせてb.bbの値が変化する" in {
    val targetDir = s"test_run_dir/$topName/btest"
    val args = defaultArgs ++ Array(
      s"--top-name=$topName",
      s"--target-dir=$targetDir"
    )

    Driver.execute(args, () => new BundleTestModule3(true)) {
      c => new PeekPokeTester(c) {
        reset()

        val b = c.io.b.get

        expect(b.bb, 0x87654321L.U)
        poke(b.b, true)
        step(1)
        expect(b.bb, 0x12345678.U)
        step(1)
      }
    } should be (true)
  }

  it should "c.b/c.cの値の変化に合わせてc.b/c.cの値が変化する" in {
    val targetDir = s"test_run_dir/$topName/ctest"
    val args = defaultArgs ++ Array(
      s"--top-name=$topName",
      s"--target-dir=$targetDir"
    )

    Driver.execute(args, () => new BundleTestModule3(true)) {
      c => new PeekPokeTester(c) {
        reset()

        val cc = c.io.c
        val trueExp = 0x12345678
        val falseExp = 0x87654321

        expect(cc.bb, intToUnsignedBigInt(falseExp))
        poke(cc.b, true)
        step(1)
        expect(cc.bb, intToUnsignedBigInt(trueExp))
        step(1)

        expect(cc.cc, intToUnsignedBigInt(falseExp))
        poke(cc.c, true)
        step(1)
        expect(cc.cc, intToUnsignedBigInt(trueExp))
        step(1)
      }
    } should be (true)
  }
}