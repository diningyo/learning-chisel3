// See LICENSE for license details.

import chisel3._

/**
  * Chiselの基本の型に関する確認
  */
class TestChiselTypes extends Module {
  val io = IO(new Bundle {})

  val boolA = Bool()
  //val boolB = true.B
  val boolB = Wire(boolA) & true.B // こちらを有効にするとエラー発生
  val boolC = false.B

  // printlnを使うとエラボレート時に型の情報が出力される
  println(boolA)
  println(boolB)
  println(boolC)

  // printfでシミューション実行時の値を出力可能
  //printf("boolA : %d\n", boolA) // これはエラーになる
  printf("boolB : %d\n", boolB)
  printf("boolC : %d\n", boolC)

  val uintA = UInt(32.W)
  val uintB = "h100".U
  val uintC = "b1010".U(32.W)

  val sintA = SInt(32.W)
  val sintB = -100.S
  val sintC = "o1010".U(32.W)

  println(uintA)
  println(uintB)
  println(s" - bit width = ${uintB.getWidth}")
  println(uintC)
  println(s" - bit width = ${uintC.getWidth}")
  println(sintA)
  println(sintB)
  println(sintC)

  // printf("uintA : %d\n", uintA) // これもエラーになる
  printf("uintB : %d\n", boolB)
  printf("uintC : %d\n", boolC)
  // printf("sintA : %d\n", sintA) // これはエラーになる
  printf("sintB : %d\n", sintB)
  printf("sintB : %b\n", sintB)
  printf("sintC : %d\n", sintC)

  val a = Wire(Bool())
  a:=true.B
}

/**
  * Chiselのモジュール基本形
  * このモジュールは入力"in"を出力"out"に接続しスルーするだけ
  */
class TestChiselAggregateTypes extends Module {
  val io = IO(new Bundle {})

  val boolVecA = Vec(2, Bool())
  val boolVecB = RegNext(VecInit(Seq(true.B, false.B)))

  // printlnを使うとエラボレート時に型の情報が出力される
  println(boolVecA)
  println(boolVecB)

  printf("boolVecA(0) = %d, boolVecB(1) = %d", boolVecA(0), boolVecA(1))
  printf("boolVecB(0) = %d, boolVecB(1) = %d", boolVecB(0), boolVecB(1))
}



/**
  * サブモジュール
  * 　入力信号を1cycle遅延させるだけ
  */
class TestChiselHardwareA extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  val regNext = RegNext(io.in, false.B)

  io.out := regNext
}


/**
  * トップモジュール
  * 　これも入力を1cycle遅延させるだけ
  */
class TestChiselHardwareTop extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  val modA = Module(new TestChiselHardwareA)
  val wireIn = Wire(Bool())
  val regInitOut = RegInit(true.B)

  // 信号を接続
  // 解説してないけど、既にお気づきの通り
  // ":="で信号を接続出来る
  wireIn := io.in
  regInitOut := modA.io.out

  modA.io.in := wireIn
  io.out := regInitOut
}

/**
  * TestChiselHardwareTopのエラボレート
  */
object ElaborateTestChiselHardwareTop extends App {
  // RTL生成用
  chisel3.Driver.execute(Array(
    "--target-dir=rtl/chisel-basic"
  ),
  () => new TestChiselHardwareTop
  )
}
