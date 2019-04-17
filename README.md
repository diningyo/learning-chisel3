# learning-chisel3

自分で書いたChiselのコードで後々見返したくなりそうなやつを集めておくリポジトリ。
大体はブログで取り上げてコードを一部掲載したものが多くなるはず。

## 必要なもの

以下の２つが動く環境

- Scala
- sbt

## 実行の方法

通常のsbt使ったプロジェクトと同様。<br>
基本的にトピック毎にサブプロジェクトを作成していく予定なので、動かしたいプロジェクトに切り替えて実行するのが良い気がする。

## 現在登録されているプロジェクト

- [chiselFlatSpec](#chiselFlatSpec)
- [chiselFlasSpecWithArgs](#chiselFlasSpecWithArgs)
- [treadleOrFirrtlSim](#treadleOrFirrtlSim)
- [xorShift](#xorShift)
- [bundleAlias](#bundleAlias)
- [uintAndSIntShift](#uintAndSIntShift)

### chiselFlatSpec
その名の通りChiselFlatSpecについて調査した際にサンプルとして作成したプロジェクト。
以下で実行可能なはず。

```bash
sbt "project chiselFlatSpec"
sbt "test"
```

### chiselFlasSpecWithArgs
ChiselFlatSpec使った形式のテストを実行する際にプログラム引数を渡す方法が無いかを試したプロジェクト。
Chiselのモジュール自体はChselFlatSpecプロジェクトと一緒で、テストモジュールに引数処理部分を追加したもの。
単純に実行するだけなら、以下で可能。

```bash
sbt "project chiselFlatSpecWithArgs"
sbt "test"
```

プログラム引数を使う場合は以下。

```bash
sbt "testOnly MyTester -- -D--backend-name=firrtl -D--generate-vcd-output=on -D--is-verbose=true"
```

### treadleOrFirrtlSim

ChiselのPeekPokeTesterを使った試験の際に遭遇した挙動の調査を行うためのサブプロジェクト（作りかけ）

```bash
sbt "project chiselFlatSpecWithArgs"
sbt "test"
```

プログラム引数を使う場合は以下。

```bash
sbt "testOnly MyTester -- -D--backend-name=firrtl -D--generate-vcd-output=on -D--is-verbose=true"
```

### xorShift

XorShiftを使った乱数生成回路用のサブプロジェクト。
Chiselのテスト用メモリのI/Fのタイミングをランダムにするために作ったやつ。
　→　普通にVerilogで書いて埋めたほうが楽な気はするがそこは気にしない。
せっかくなので、ベタ書きのXorShift32版を元にリファクタリングしてChiselっぽく書き換えていくことにする。

```bash
sbt "project xorShift"
sbt "test"
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
sbt "project bundleAlias"
sbt "test"
```

### uintAndSIntShift

Chiselで算術右シフトのやり方をキャストをした際の挙動を調べたもの。

```bash
sbt "project uintAndSIntShift"
sbt "test"
```

### parallelTestExecution

ScalaTestの`ParallelTestExecution`を使ってChiselFlatSpecを継承して作った
テストクラス内のテストを並列実行する処理を確認するためのサブプロジェクト。

```bash
sbt "project parallelTestExecution"
```

#### 逐次実行する場合のコマンド

```bash
sbt "testOnly SequentialTester"
```

#### 並列実行する場合のコマンド

```bash
testOnly ParallelTester
```