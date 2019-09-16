import 'dart:convert';
import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/local_storage.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:flutter/material.dart';


void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'BFN WebApp',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.indigo,
      ),
      home: MyHomePage(title: 'BFN - Business Finance Network 2019'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;
  bool isBusy = false;
  List<AccountInfo> accounts = List();
  List<Invoice> invoices = List();
  List<InvoiceOffer> invoiceOffers = List();
  var text = 'BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019 BFN - Business Finance Network 2019';
  @override
  initState() {
    super.initState();
    _ping();
  }

  _ping() async {
    print('\n\n🎽 🎽  🎽 🎽  🎽 🎽  🎽 🎽  🎽 🎽  ping Corda node 🍊 🍊 🍊\n\n');
    var res = await Net.ping();
    print(res);
    var m = await Prefs.getDemoBoolean();
    print('🎽 🎽 demo boolean: $m');
  }
  _startDemoData() async {
    print(' 🎽 🎽  🎽 🎽  🎽 🎽  🎽 🎽  🎽 🎽  start startDemoDataGeneration');
    setState(() {
      isBusy = true;
    });
    var result = await Net.startDemoDataGeneration();
    print(result);
    setState(() {
      isBusy = false;
    });
  }
  _getNet() async {
    accounts.clear();
    invoices.clear();

    var ping = await Net.ping();
    print(ping);

    var cnt = 0;
    String result = await Net.getAccounts();
    List map = json.decode(result);
    print('🏈  🏈  🏈  🏈  about to print accounts received from corda ...  🏈  🏈  🏈  🏈 ');
    map.forEach((f) {

      accounts.add(AccountInfo.fromJson(f));
    });
    print(
        '🧩 🧩 🧩 🧩 🧩 🧩 🧩  getAccounts found  💜 ${accounts.length}  💜 accounts on corda node  🧩 🧩 🧩 🧩');
    accounts.forEach((acc) {
      cnt++;
      print('🧩 🧩 account: 👽 👽 #$cnt ' + acc.toJson().toString() + " 🧩 ");
    });
    print('🏈  🏈  🏈  🏈  completed printing accounts...  🏈  🏈  🏈  🏈 ');

    cnt = 0;
    String result1 = await Net.getInvoices();
    List map1 = json.decode(result1);
    print('\n\n🏈  🏈  🏈  🏈  about to print invoices received from corda ...  🏈  🏈  🏈  🏈 ');
    map1.forEach((f) {
      invoices.add(Invoice.fromJson(f));
    });
    print(
        '🍎 🍎 🍎 🍎 🍎 🍎 🍎   getInvoices found  💜 ${invoices.length}  💜 invoices on corda node  🍎 🍎 🍎 🍎 ');
    invoices.forEach((acc) {
      cnt++;
      print('🍎 🍎 invoice: 🌽 #$cnt ' + acc.toJson().toString() + " 🍎 ");
    });
    print('🏈  🏈  🏈  🏈  completed printing invoices...  🏈  🏈  🏈  🏈 ');

    cnt = 0;
    String result2 = await Net.getInvoiceOffers();
    List map2 = json.decode(result2);
    print('\n\n🏀 🏀 🏀 🏀 🏀  about to print invoiceOffers received from corda ... 🏀 🏀 🏀 🏀 ');
    map2.forEach((f) {
      invoiceOffers.add(InvoiceOffer.fromJson(f));
    });
    print(
        '🎽 🎽 🎽 🎽 🎽 🎽 🎽   getInvoiceOffers found  💜 ${invoiceOffers.length}  💜 invoiceOffers on corda node  🎽 🎽 🎽 🎽 ');
    invoiceOffers.forEach((acc) {
      cnt++;
      print('🥦 🥦  invoiceOffer: 🍊 #$cnt ' + acc.toJson().toString() + " 🥦 ");
    });
    print('🏀 🏀 🏀 🏀   completed printing invoiceOffers...  🏀 🏀 🏀 🏀 ');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        bottom: PreferredSize(child: Column(), preferredSize: Size.fromHeight(200)),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            SizedBox(height: 24,),
            isBusy? Container(): RaisedButton(
              onPressed: _startDemoData,
              color: Colors.pink,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Text('Start Demo Data Generation', style: TextStyle(color: Colors.white,
                fontSize: 20),),
              ),
            ),
            RaisedButton(
              onPressed: _getNet,
              color: Colors.blue[600],
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Text('Get Corda Node Data', style: TextStyle(color: Colors.white,
                    fontSize: 20),),
              ),
            ),
//          GridView.count(crossAxisCount: 3,
//          children: <Widget>[
//            Card(
//              elevation: 8,
//              color: Colors.teal[100],
//              child: Container(
//                width: 120,height: 120,
//              ),
//            ),
//            Card(
//              elevation: 8,
//              color: Colors.purple[100],
//              child: Container(
//                width: 120,height: 120,
//              ),
//            ),
//            Card(
//              elevation: 8,
//              color: Colors.red[100],
//              child: Container(
//                width: 120,height: 120,
//              ),
//            ),
//            Card(
//              elevation: 8,
//              color: Colors.orange[100],
//              child: Container(
//                width: 120,height: 120,
//              ),
//            ),
//            Card(
//              elevation: 8,
//              color: Colors.cyan[100],
//              child: Container(
//                width: 120,height: 120,
//              ),
//            ),
//          ],),
            Card(
              child: Container(
                width: 400, height: 400,
                color: Colors.amber[300],
                child: Padding(
                  padding: const EdgeInsets.all(20.0),
                  child: Text(text),
                ),
              ),
            )
          ],
        ),
      ),
//      floatingActionButton: FloatingActionButton(
//        onPressed: _incrementCounter,
//        tooltip: 'Increment',
//        child: Icon(Icons.add),
//      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
