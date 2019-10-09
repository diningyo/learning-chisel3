# learning-chisel3

自分で書いたChiselのコードで後々見返したくなりそうなやつを集めておくリポジトリ。
大体はブログで取り上げてコードを一部掲載したものが多くなるはず。

## 必要なもの

以下の３つが動く環境

- Scala
- sbt - 1.2.7
- verilator - 手元のバージョンは4.012

## 実行の方法

通常のsbt使ったプロジェクトと同様。<br>
基本的にトピック毎にサブプロジェクトを作成していく予定なので、動かしたいプロジェクトに切り替えて実行するのが良い気がする。
なお以下に書いているコマンドは`sbt`を起動した状態で入力する形で記載しているので注意。

## 現在登録されているプロジェクト

- [chiselFlatSpec](#chiselFlatSpec)
- [chiselFlasSpecWithArgs](#chiselFlasSpecWithArgs)
- [treadleOrFirrtlSim](#treadleOrFirrtlSim)
- [xorShift](#xorShift)
- [bundleAlias](#bundleAlias)
- [uintAndSIntShift](#uintAndSIntShift)
- [parallelTestExecution](#parallelTestExecution)
- [loadChiselMem](#loadChiselMem)
- [memND](#memND)
- [loadChiselMem](#loadChiselMem)
- [bareAPICall](#bareAPICall)
- [utilQueue](#utilQueue)
- [chiselName](#chiselName)
- [trialNIC](#trialNIC)
- [MultiIOModule](#multiIOModule)
- [blackboxCheck](#blackboxCheck)

### chiselFlatSpec
その名の通りChiselFlatSpecについて調査した際にサンプルとして作成したプロジェクト。
以下で実行可能なはず。

```bash
project chiselFlatSpec
test
```

### chiselFlasSpecWithArgs
ChiselFlatSpec使った形式のテストを実行する際にプログラム引数を渡す方法が無いかを試したプロジェクト。
Chiselのモジュール自体はChselFlatSpecプロジェクトと一緒で、テストモジュールに引数処理部分を追加したもの。
単純に実行するだけなら、以下で可能。

```bash
project chiselFlatSpecWithArgs
test
```

プログラム引数を使う場合は以下。

```bash
testOnly MyTester -- -D--backend-name=firrtl -D--generate-vcd-output=on -D--is-verbose=true
```

### treadleOrFirrtlSim

ChiselのPeekPokeTesterを使った試験の際に遭遇した挙動の調査を行うためのサブプロジェクト（作りかけ）

```bash
project chiselFlatSpecWithArgs
test
```

プログラム引数を使う場合は以下。

```bash
testOnly MyTester -- -D--backend-name=firrtl -D--generate-vcd-output=on -D--is-verbose=true
```

### xorShift

XorShiftを使った乱数生成回路用のサブプロジェクト。
Chiselのテスト用メモリのI/Fのタイミングをランダムにするために作ったやつ。
　→　普通にVerilogで書いて埋めたほうが楽な気はするがそこは気にしない。
せっかくなので、ベタ書きのXorShift32版を元にリファクタリングしてChiselっぽく書き換えていくことにする。

```bash
project xorShift
test
```

### bundleAlias

`Bundle`を使ってデータを構造化した場合に、`Bundle`配下のデータの名前が長いな、、どうにか出来るよね、これ。
というのを確認するためのサブプロジェクト。

例えば、わざとらしいけど以下のような`Bundle`を考えた時に`io.a.bb.ccc`にアクセスしやすくすることが出来るかを確かめる

```scala
class A(hasOptPort: Boolean = true) extends Bundle {
  val a = new Bundle {
    val bb = new Bundle {
      val ccc = new Bundle {
        val d = Input(Bool())
        val e = Output(Bool())
        val f = Input(UInt(32.W))
        val g = if (hasOptPort) Some(Output(UInt(32.W))) else None
      }
    }
  }
}

class B(hasOptPort: Boolean = true) extends Module {
  val io = IO(new A)

  io.a.bb.ccc.e := io.a.bb.ccc.d       // Bundleで構造化していくと
  if (hasOptPort) {
    io.a.bb.ccc.g.get := io.a.bb.ccc.f // どんどん深くなっていく
  }
}
```

```bash
project bundleAlias
test
```

### uintAndSIntShift

Chiselで算術右シフトのやり方をキャストをした際の挙動を調べたもの。

```bash
project uintAndSIntShift
test
```

### parallelTestExecution

ScalaTestの`ParallelTestExecution`を使ってChiselFlatSpecを継承して作った
テストクラス内のテストを並列実行する処理を確認するためのサブプロジェクト。

```bash
project parallelTestExecution
```

#### 逐次実行する場合のコマンド

```bash
testOnly SequentialTester
```

#### 並列実行する場合のコマンド

```bash
testOnly ParallelTester
```

### loadChiselMem

Chiselのメモリにファイルから読み込んだデータを設定する機能を試した時のサブプロジェクト。

```bash
project loadChiselMem
testOnly MemoryTester
```

### memND

Chiselの`Mem`を使って2次元のメモリを作る方法を間検討した時に作成したサブプロジェクト。<br>
System Verilogの以下の記述で出来ることをChiselで実現してみた。

```verilog
reg [3:0][7:0] mem[0:1023]
```

#### RTLの生成コマンド

以下のコマンドで"subprj/mem-nd/src/main/scala/Mem2D.scala"内のMem2DクラスのRTLが生成される。<br>

```bash
project memND
runMain ElaborateMem2D
```

生成したRTLは以下のディレクトリに格納される。"WithWrite"と入っているファイルは`MemBase`クラスの`write`を使って書いた場合のRTLとなる。

- rtl/mem2d
  - Mem2D.anno.json
  - Mem2D.fir
  - Mem2D.v
  - Mem2DWithWrite.anno.json
  - Mem2DWithWrite.fir
  - Mem2DWithWrite.v

#### テスト実行コマンド

以下のコマンドでテストが実行される。

```bash
 testOnly Mem2DTester
```

### bareAPICall

モジュールのテストを作成していて出くわしたエラーに対しての確認用のプロジェクト。

```bash
project bareAPICall
runMain TestElaborateBeforeErrorMod
runMain TestElaborateRegenerateErrorModFail
runMain TestElaborateRegenerateErrorModOK
```

コードから抜粋して書くと、以下のようなコードを作るとエラボレートは通るがテスト時の`expect`でエラーになる。

```scala
class RegenerateErrorMod extends Module {
  val io = IO(new Bundle {
    val out = Output(UInt(2.W))
  })

  val out = Wire(Vec(2, Bool()))

  out(0) := true.B
  out(1) := false.B

  io.out := out.asUInt()
}

object TestElaborateRegenerateErrorModFail extends App {
  //
  iotesters.Driver.execute(args, () => new RegenerateErrorMod()) {
    c => new PeekPokeTester(c) {
      // ここでUIntの各ビットを参照するとエラー
      expect(c.io.out(0), true)
      expect(c.io.out(1), true)
    }
  }
}
```

### utilQueue

Chiselに含まれる`util.Queue`のオプションによる挙動の違いを調査した際のもので、以下のブログ記事の確認コード。
調査したオプションはQueueの第３/第４になっている`pipe/flow`。

- [Chiselのutil.Queueの使い方の再確認 ](https://www.tech-diningyo.info/entry/2019/07/07/224321)

```bash
project utilQueue
test
```

上記のテストコマンドを実行すると"test_run_dir"に以下の４つのディレクトリが生成され、ダンプした波形が確認可能。

1. QueuePipeOffFlowOff
1. QueuePipeOffFlowOn
1. QueuePipeOnFlowOff
1. QueuePipeOnFlowOn

### chiselName

Chiselに入っているアノテーション`@chiselName`の効果を確認するためのプロジェクト。
対応するのは以下の記事。

- [Chiselの便利なアノテーション@chiselNameを試してみた ](https://www.tech-diningyo.info/entry/2019/07/08/233526)

```scala
@chiselName
class TestMod extends Module {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val b = Input(Bool())
    val c = Output(UInt(4.W))
  })
  when (io.a) {
    val innerReg = RegInit(5.U(4.W)) // こういうブロック内の変数に名前がつけられる機能
    innerReg := innerReg + 1.U
    when (io.b) {
      val innerRegB = innerReg + 1.U
      innerReg := innerRegB
    }
    io.c := innerReg
  } .otherwise {
    io.c := 10.U
  }
}

object Elaborate extends App {
  Driver.execute(Array(
    "-tn=TestMod",
    "-td=rtl/chiselName"
  ),
  () => new TestMod)
}
```

- エラボレートの実行
```bash
project chiselName
runMain Elaborate
```

上記を実行すると

- rtl/chiselName

にFIRRTL/RTLが生成されます。

### trialNIC

Chiselのパラメタライズのお試し実装として作った簡単なNIC用のプロジェクト。
以下のブログ記事で紹介したもの。
記事の方では割愛したテストも含んでいます。

- [Chiselで作るNIC - （１）- 仕様について](https://www.tech-diningyo.info/entry/2019/07/21/212610)
- [Chiselで作るNIC - （2）- Decoder](https://www.tech-diningyo.info/entry/2019/07/21/214526)
- [Chiselで作るNIC - （3）- Arbiter](https://www.tech-diningyo.info/entry/2019/07/23/224045)
- [Chiselで作るNIC - （4）- トップモジュール](https://www.tech-diningyo.info/entry/2019/07/24/232828)

sbtのコマンドラインから以下を実行してプロジェクトを切り替えてください。

```
project trialNIC
```

#### テストの実行

tiralNICプロジェクトを選択した状態で、以下で各モジュール毎のテストを実行することが可能です。
各テストの内容についてはテスト用クラスの実装をご覧ください。

- NICDecoder

```
testOnly testOnly NICArbiterTester
```

- NICArbiter

```
testOnly NICArbiterTester
```

- NICTop

```
testOnly NICTopTester
```

### MultiIOModule

`MultiIOModule`を使ったデバッグポート作成に関するサンプルプロジェクト

以下のブログ記事の内容

[MultiIOModuleを使ったデバッグ用ポートについての作成](https://www.tech-diningyo.info/entry/2019/09/15/214611)

```bash
project multiIOModule
runMain Test
```

コードから抜粋して書くと、以下のようなコードでデバッグ用のポートを`dbg_`という接頭辞で作れる。

```scala
/**
  * デバッグポート用のBundle
  * @param bits
  */
class DebugIO(bits: Int) extends Bundle {
  val count = Output(UInt(bits.W))
  override def cloneType: this.type = new DebugIO(bits).asInstanceOf[this.type]
}

/**
  * MultiIOModule使ったデバッグポート作成のサンプル
  * @param debug
  */
class DebugWithMultiIOModule(debug: Boolean) extends MultiIOModule {

  val bits = log2Ceil(4)

  val io = IO(new Bundle {
    val in = Flipped(new SomeDataIO)
    val out = new SomeDataIO
  })
  val q = Module(new Queue(chiselTypeOf(io.in.bits), 4))

  q.io.enq <> io.in
  io.out <> q.io.deq

  // debug
  val dbgBits = if (debug) 32 else 0
  val dbg = IO(new DebugIO(dbgBits))

  dbg.count := q.io.count
}
```

### blackboxCheck

`BlackBox`でVerilog-HDLの階層化されたモジュールを読み込む方法を確認したプロジェクト

以下のブログ記事の内容

[ChiselのBlackboxで複数のファイルから構成されるVerilog-HDLのモジュールを読み込む](https://www.tech-diningyo.info/entry/2019/10/07/235812)

```bash
project blackboxCheck
test
```