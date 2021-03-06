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
- [bareAPICall](#bareAPICall)
- [utilQueue](#utilQueue)
- [chiselName](#chiselName)
- [trialNIC](#trialNIC)
- [MultiIOModule](#multiIOModule)
- [blackboxCheck](#blackboxCheck)
- [arbiterTest](#arbiterTest)
- [simWDT](#simWDT)
- [chisel32x](#chisel32x)
- [chisel33x](#chisel33x)

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
import chisel3._
import chisel3.util._

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
import chisel3._
import chisel3.util._

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
import chisel3._
import chisel3.util._

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
import chisel3._
import chisel3.util._

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

### arbiterTest

RRArbiterでReady-Validのハンドシェイクのテストコードをどう書けばいいか確認した際のコード。

以下のブログ記事の内容

[ChiselのArbiterのvalid/readyの調停テストコードが上手く作れなかった話](https://www.tech-diningyo.info/entry/2019/07/28/122222)

```bash
project arbiterTest
test
```

実行すると以下のように１つ目のテストがFAILします。これは意図通りに動かないというダメだった例を残してあるものです。

```bash
[info] ArbiterTester:
[info] RRArbiter
[info] - should 以下のテストを実行するとin(2)/in(3)のvalidが同時にLOWに落ちる *** FAILED ***
[info]   false was not true (ArbiterTester.scala:41)
[info] - should in(N).readyを取得してからvalidを制御するとうまくいく
[info] ScalaTest
[info] Run completed in 2 seconds, 41 milliseconds.
[info] Total number of tests run: 2
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 1, canceled 0, ignored 0, pending 0
[info] *** 1 TEST FAILED ***
[error] Failed: Total 2, Failed 1, Errors 0, Passed 1
[error] Failed tests:
[error] 	ArbiterTester
[error] (Test / test) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 8 s, completed 2019/10/09 23:50:45
```

### simWDT

Chiselのテスト環境を使用したテストを実行する際に、テストベンチのモジュール側にタイマーを入れてシミュレーションをタイムアウトする仕組みの確認コード。

#### 概要

以下のような感じでタイマーをインスタンスした基底クラスを用意しておき、実際のテストベンチはこの基底クラスを継承して作成する。

```scala
import chisel3._
import chisel3.util._

/**
  * シミュレーションのトップモジュール
  * @param limit シミュレーションのMAXサイクル数
  * @param abortEn timeout時にassertでシミュレーションを終了するかどうか
  */
abstract class BaseSimDTM(limit: Int, abortEn: Boolean = true) extends Module {
  val io: BaseSimDTMIO
  val wdt = Module(new WDT(limit, abortEn))

  def connect(finish: Bool): Unit = {
    io.finish := finish
    io.timeout := wdt.io.timeout
  }
}

/**
  * テスト用のシミュレーショントップモジュール
  * @param limit シミュレーションのMAXサイクル数
  * @param abortEn timeout時にassertでシミュレーションを終了するかどうか
  */
class SimDTM(limit: Int, abortEn: Boolean = true) extends BaseSimDTM(limit, abortEn) {
  val io = IO(new Bundle with BaseSimDTMIO)

  connect(false.B)
}
```

ブログの記事は以下。

[Chiselのシミュレーションを所定のサイクルで終了する方法](https://www.tech-diningyo.info/entry/2019/08/01/233811)

#### 実行方法

```bash
project simWDT
test
```

- 実行結果
    - 以下のように時間が来ると`assert`が発火してシミュレーションが終了する。

```bash
[info] [0.002] Elaborating design...
[info] [0.132] Done elaborating.
Total FIRRTL Compile Time: 440.7 ms
file loaded in 0.075331792 seconds, 24 symbols, 20 statements
[info] [0.001] SEED 1570717992611
[info] [0.002] step = 0
[info] [0.003] step = 1
[info] [0.003] step = 2
[info] [0.004] step = 3
[info] [0.004] step = 4
[info] [0.005] step = 5
[info] [0.005] step = 6
[info] [0.005] step = 7
[info] [0.006] step = 8
[info] [0.006] step = 9
[info] [0.006] step = 10
[info] [0.007] step = 11
[info] [0.007] step = 12
[info] [0.008] step = 13
[info] [0.008] step = 14
[info] [0.009] step = 15
[info] [0.009] step = 16
[info] [0.010] step = 17
[info] [0.011] step = 18
[info] [0.011] step = 19
Assertion failed: WDT is expired!!
    at BaseSimDTM.scala:25 assert(!timeout, "WDT is expired!!")
treadle.executable.StopException: Failure Stop: result 1
```

### chisel32x

Chisel 3.2.0で変更のあった部分で気になる部分を確認した際のコード。
以下の機能の確認コードが含まれる。

- [Mux使用時のDontCare指定](#Mux使用時のDontCare指定)
- [BundleLiterals](#BundleLiterals)
- [Verilog形式のメモリ読み込みのサポート](#Verilog形式のメモリ読み込み)
- [MixedVecのサポート](#MixedVec)
- [Strong enumsのサポート](#Strongenumsのサポート)
- [HasBlackBoxPathの追加](#HasBlackBoxPath)
- [非同期リセットのサポート](#AsyncReset)

#### プロジェクトの切り替え

```bash
project chisel32x
```

#### Mux使用時のDontCare指定

```bash
runMain ElaborateMuxDontCare
```

正常にエラボレートが終了し以下の2つのRTLが生成される。

- rtl/chisel-3.2.0/MuxDontCare/MuxDontCare.v
- rtl/chisel-3.2.x/MuxCaseDontCare/MuxCaseDontCare.v

#### BundleLiterals

- [subprj/chisel-3.2.x/src/main/TrialBundleLiterals.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.2.x/src/main/scala/TrialBundleLiterals.scala)

```bash
runMain ElaborateBundleLiterals
```

以下のRTLが生成される。

- rtl/chisel-3.2.x/TrialBundleLiterals/TrialBundleLiterals.v

#### Verilog形式のメモリ読み込みのサポート

以下がテスト用のコード

- [subprj/chisel-3.2.x/src/main/MemVerilogStyle.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.2.x/src/main/scala/MemVerilogStyle.scala)
- [subprj/chisel-3.2.x/src/test/MemVerilogStyleTester.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.2.x/src/test/scala/MemVerilogStyleTester.scala)

```bash
testOnly MemVerilogStyleTester
```

テスト実行結果は以下のようになる。

```bash
STARTING test_run_dir/chisel-3.2.x/MemVerilogStyle/MemVerilogStyleTester453628597/VMemVerilogStyle
[info] [0.002] SEED 1571625549898
[info] [0.011] c.io.rddata = 0x00000000
[info] [0.014] c.io.rddata = 0x00000001 // @によるアドレス指定で"1"だけずれて配置
[info] [0.015] c.io.rddata = 0x00000002
[info] [0.015] c.io.rddata = 0x00000000
[info] [0.017] c.io.rddata = 0x00000000
[info] [0.018] c.io.rddata = 0x00000000
[info] [0.019] c.io.rddata = 0x00000000
[info] [0.020] c.io.rddata = 0x00000000
```

#### MixedVec

`MixedVec`の使い方の確認

- [subprj/chisel-3.2.x/src/main/TrialMixedVec.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.2.x/src/main/scala/TrialMixedVec.scala)

```bash
runMain ElaborateMixedVec
```

以下のRTLが生成される

- rtl/chisel-3.2.x/BundleLiterals/TrialMixedVec.v

#### Strong enumsのサポート

"Strong Enums"と紹介されている`ChiselEnum`の確認コード

- [subprj/chisel-3.2.x/src/main/TrialStrongEnums.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.2.x/src/main/scala/TrialStrongEnums.scala)

```bash
runMain ElaborateStrongEnums
```

- rtl/chisel-3.2.x/TrialStrongEnums/TrialStrongEnums.v

#### HasBlackBoxPath

`HasBlackBoxPath`に実装された`addPath`メソッドの動作確認のコード。`sbt`のディレクトリ構成で固定される`{main, test}/resources/`以外のパスにRTLを置いても読み込むことが可能。

```bash
testOnly TrialHasBlackBoxPathTester
```

シミュレーションの実行結果は以下のようにエラー無く終了する。

```bash
[info] TrialHasBlackBoxPathTester:
[info] - should addPathで指定したパスのRTLが読み込める
[info] ScalaTest
[info] Run completed in 2 seconds, 804 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[info] Passed: Total 1, Failed 0, Errors 0, Passed 1
```

#### AsyncReset

非同期リセットの確認コード

- [subprj/chisel-3.2.x/src/main/TrialAsyncReset.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.2.x/src/main/scala/TrialAsyncReset.scala)

```bash
runMain ElaborateTrialAsyncReset
```

- rtl/chisel-3.2.x/TrialAsyncReset/TrialAsyncReset.v

### chisel33x

Chisel 3.3.0で変更のあった部分で気になる部分を確認した際のコード。
以下の機能の確認コードが含まれる。

- [RTL生成方法の変更（Driver.execute→ChiselStage）](#RTL生成方法の変更（Driver.execute→ChiselStage）)
- [BundleLiterals](#BundleLiterals)
- [ChiselのSyncReadMemのアクセス競合時の動作に関するオプションの追加](#ChiselのSyncReadMemのアクセス競合時の動作に関するオプションの追加)
- [@chiselNameの強化](#@chiselNameの強化)
- [BitPatが空白をサポート](#BitPatが空白をサポート)
- [FixedPointの拡張](#FixedPointの拡張)
- [printfがTABをサポート](#printfがTABをサポート)

#### プロジェクトの切り替え

```bash
project chisel33x
```

#### RTL生成方法の変更（Driver.execute→ChiselStage）

Chisel 3.3.0からChiselのソースコードのエラボレート方法が、`chisel3.Driver.execute`から`chisel3.stage.ChiselStage`を使う形に変更になった。

- [subprj/chisel-3.3.x/src/main/Elaborate.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.3.x/src/main/scala/Elaborate.scala)

```bash
runMain OldElaborate # Chisel 3.2.xまでの方法
runMain NewElaborate # Chisel 3.3.0からの方法
```

参考：[Chisel3.3.0のリリースノートを確認した（2） - ChiselStageを使ったエラボレート ](https://www.tech-diningyo.info/entry/2020/05/17/164148)

#### ChiselのSyncReadMemのアクセス競合時の動作に関するオプションの追加

Chiselの`SyncReadMem`に`ruw`という引数が追加され、同一アドレスへのアクセス競合時の動作を指定できるようになった。

- [subprj/chisel-3.3.x/src/main/SyncReadMemAccCollision.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.3.x/src/main/scala/SyncReadMemAccCollision.scala)

```bash
runMain ElaborateSRMA # RTLの生成
runMain TestSRMA # テストの実行
```

参考：[Chisel3.3.0のリリースノートを確認した（3） - SyncReadMemに追加された引数](https://www.tech-diningyo.info/entry/2020/07/26/225448)

#### @chiselNameの強化

アノテーション`@chiselName`の機能が強化され、Chiselの型以外のクラスについてもアノテーションの効果が得られるようになった。
また、`NoChiselNamePrefix`という`@chiselName`の効果を打ち消すトレイトが追加された。このトレイトを適用することで、冗長な名前が生成されるのを抑制できる。

- Non-`Bundle`な通常のクラスに対しての`@chiselName`の効果を確認するコード：[subprj/chisel-3.3.0/src/main/TestChiselName1.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.3.x/src/main/scala/TestChiselName1TrialAsyncReset.scala)
- `NoChiselNamePrefix`の効果を確認するコード：[subprj/chisel-3.3.0/src/main/TestChiselName2.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.3.x/src/main/scala/TestChiselName2.scala)

```bash
runMain ElaborateChiselName # Non-Bundleのクラスに対しての@chiselNameの確認
runMain ElaborateNoChiselNamePrefix # NoChiselNamePrefixの確認
```

- 参考
  - [Chisel3.3.0のリリースノートを確認した（4） - @chiselNameが強化された](https://www.tech-diningyo.info/entry/2020/09/06/233726)
  - [Chisel3.3.0のリリースノートを確認した（5） - NoChiselNamePrefix](https://www.tech-diningyo.info/entry/2020/09/22/113227)

#### BitPatが空白をサポート

`BitPat`でセパレータとして空白` `がサポートされた。

- [subprj/chisel-3.3.x/src/main/TrialAsyncReset.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.3.x/src/main/scala/TrialAsyncReset.scala)

```bash
runMain ElaborateBitPatWithWS
```

参考：[Chisel3.3.0のリリースノートを確認した（6） - 簡単なやつを３つ](https://www.tech-diningyo.info/entry/2020/09/30/224930#1283-BitPat%E3%81%A7%E7%A9%BA%E7%99%BD%E6%96%87%E5%AD%97%E3%81%8C%E4%BD%BF%E3%81%88%E3%82%8B%E3%82%88%E3%81%86%E3%81%AB%E3%81%AA%E3%81%A3%E3%81%9F)

#### FixedPointの拡張

`FixedPoint`が拡張され、`Double`や`BigDecimal`からの変換が出来るようになった。

- [subprj/chisel-3.2.x/src/main/ExpandToFixedPoint.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.3.x/src/main/scala/ExpandToFixedPoint.scala)

```bash
runMain ExpandToFixedPoint
```

参考：[Chisel3.3.0のリリースノートを確認した（6） - 簡単なやつを３つ](https://www.tech-diningyo.info/entry/2020/09/30/224930#1284-Scala%E3%81%AEDoubleBigDecimal%E3%81%8B%E3%82%89FixedPoint%E3%81%B8%E3%81%AE%E5%A4%89%E6%8F%9B%E3%81%8C%E3%81%A7%E3%81%8D%E3%82%8B%E3%82%88%E3%81%86%E3%81%AB%E3%81%AA%E3%81%A3%E3%81%9F)

#### printfがTABをサポート

`printf`でTAB文字`\t`がサポートされた

- [subprj/chisel-3.3.x/src/main/TrialAsyncReset.scala](https://github.com/diningyo/learning-chisel3/blob/master/subprj/chisel-3.3.x/src/main/scala/TrialAsyncReset.scala)

```bash
runMain ElaborateTrialAsyncReset
```

参考：[Chisel3.3.0のリリースノートを確認した（6） - 簡単なやつを３つ](https://www.tech-diningyo.info/entry/2020/09/30/224930#1326-Printf%E3%81%8C%E3%82%BF%E3%83%96%E6%96%87%E5%AD%97%E3%82%92%E3%82%B5%E3%83%9D%E3%83%BC%E3%83%88%E3%81%97%E3%81%9F)
