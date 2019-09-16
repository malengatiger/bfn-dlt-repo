import 'dart:convert';
import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:flutter/material.dart';


void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'BFN WebApp',
      theme: ThemeData(
        primarySwatch: Colors.blue,
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
  List<AccountInfo> accounts = List();
  List<Invoice> invoices = List();
  List<InvoiceOffer> invoiceOffers = List();

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
    _getNet();
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
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              'You have pushed the button this many times:',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.display1,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
