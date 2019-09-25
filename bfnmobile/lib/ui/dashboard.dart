import 'dart:convert';
import 'dart:io';

import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/data/node_info.dart';
import 'package:bfnlibrary/util/functions.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:bfnlibrary/util/prefs.dart';
import 'package:bfnlibrary/util/slide_right.dart';
import 'package:bfnlibrary/util/snack.dart';
import 'package:bfnmobile/ui/list_tabs.dart';
import 'package:bfnmobile/ui/network_accounts.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';

import 'file:///Users/aubs/WORK/CORDA/bfn-dlt-repo/bfnmobile/lib/bloc.dart';

class Dashboard extends StatefulWidget {
  @override
  _DashboardState createState() => _DashboardState();
}

class _DashboardState extends State<Dashboard> {
  var _key = GlobalKey<ScaffoldState>();
  FirebaseMessaging _firebaseMessaging = FirebaseMessaging();
  List<AccountInfo> accounts = List();
  List<Invoice> invoices = List();
  List<InvoiceOffer> offers = List(), offerMessages = List();
  AccountInfo account;
  List<NodeInfo> nodes = List();

  @override
  void initState() {
    super.initState();
    _firebaseCloudMessaging();
    _getNodes();
    _refresh();
  }

  _getNodes() async {
    nodes = await Net.listNodes();
  }

  void _firebaseCloudMessaging() {
    print(
        '🍊 🍊 _firebaseCloudMessaging started. 🍊 Configuring messaging 🍊 🍊 🍊');
    if (Platform.isIOS) iOS_Permission();

    _firebaseMessaging.getToken().then((token) {
      print(token);
    });

    _firebaseMessaging.configure(
      onMessage: (Map<String, dynamic> message) async {
        print('🧩🧩🧩🧩🧩🧩 on message $message');
        var data = message['data'];
        var offer = json.decode(data['offer']);

        var m = InvoiceOffer.fromJson(offer);
        offerMessages.add(m);
        _showMessage('Invoice Offer ' + m.offerAmount.toString());
        _refresh();
      },
      onResume: (Map<String, dynamic> message) async {
        print('🧩🧩🧩🧩🧩🧩 on resume $message');
      },
      onLaunch: (Map<String, dynamic> message) async {
        print('🧩🧩🧩🧩🧩🧩 on launch $message');
      },
    );
    _subscribe();
  }

  void _showMessage(String msg) {
    print('showing fcm message ... $msg');
    AppSnackbar.showSnackbar(
        scaffoldKey: _key,
        message: msg,
        textColor: Colors.yellow,
        backgroundColor: Colors.black);
  }

  void _subscribe() {
    _firebaseMessaging.subscribeToTopic('invoiceOffers');
    _firebaseMessaging.subscribeToTopic('invoices');
    print('🧩🧩🧩🧩🧩🧩 subscribed to FCM topics 🍊 invoiceOffers 🍊 invoices');
  }

  void iOS_Permission() {
    _firebaseMessaging.requestNotificationPermissions(
        IosNotificationSettings(sound: true, badge: true, alert: true));
    _firebaseMessaging.onIosSettingsRegistered
        .listen((IosNotificationSettings settings) {
      print("Settings registered: $settings");
    });
  }

  _getAccounts() async {
    account = await Prefs.getAccount();
    accounts = await bfnBloc.getAccounts();
    contents.add(Content(
        label: 'Network Accounts',
        number: accounts.length.toString(),
        icon: Icon(Icons.people),
        color: Colors.teal));
    setState(() {});
  }

  _getInvoices() async {
    invoices = await bfnBloc.getInvoices();
    contents.add(Content(
        label: 'Network Invoices',
        number: invoices.length.toString(),
        icon: Icon(Icons.account_balance),
        color: Colors.blue));
    setState(() {});
  }

  _getInvoiceOffers() async {
    offers = await bfnBloc.getInvoiceOffers();
    contents.add(Content(
        label: 'Network Offers',
        number: offers.length.toString(),
        icon: Icon(Icons.apps),
        color: Colors.pink));
    setState(() {});
  }

  _refresh() async {
    setState(() {
      contents.clear();
    });
    await _getAccounts();
    await _getInvoices();
    await _getInvoiceOffers();
  }

  List<Content> contents = List();

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () => doNothing(),
      child: Scaffold(
        key: _key,
        appBar: AppBar(
          leading: Container(),
          title: Text("Business Finance Network"),
          elevation: 8,
          actions: <Widget>[
            IconButton(
              icon: Icon(Icons.refresh),
              onPressed: _refresh,
            ),
            IconButton(
              icon: Icon(Icons.person_add),
              onPressed: _changeAccount,
            ),
          ],
          bottom: PreferredSize(
              child: Column(
                children: <Widget>[
                  Text(
                    account == null ? '' : account.name,
                    style: Styles.whiteBoldMedium,
                  ),
                  SizedBox(
                    height: 20,
                  )
                ],
              ),
              preferredSize: Size.fromHeight(60)),
        ),
        backgroundColor: Colors.brown[100],
        body: GridView.builder(
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2, mainAxisSpacing: 2, crossAxisSpacing: 2),
          itemCount: contents.length,
          itemBuilder: (BuildContext context, int index) {
            return Padding(
              padding: const EdgeInsets.all(8.0),
              child: Container(
                height: 80,
                width: 160,
                child: Card(
                  elevation: 4,
                  child: Center(
                    child: Column(
                      children: <Widget>[
                        SizedBox(
                          height: 24,
                        ),
                        contents.elementAt(index).icon,
                        SizedBox(
                          height: 24,
                        ),
                        Text(
                          '${contents.elementAt(index).number}',
                          style: TextStyle(
                              fontSize: 44,
                              fontWeight: FontWeight.w900,
                              color: contents.elementAt(index).color),
                        ),
                        SizedBox(
                          height: 8,
                        ),
                        Text(contents.elementAt(index).label),
                      ],
                    ),
                  ),
                ),
              ),
            );
          },
        ),
        bottomNavigationBar: BottomNavigationBar(
          items: [
            BottomNavigationBarItem(
                icon: Icon(Icons.supervisor_account), title: Text('Accounts')),
            BottomNavigationBarItem(
                icon: Icon(Icons.apps), title: Text('Invoices')),
            BottomNavigationBarItem(
                icon: Icon(Icons.account_balance), title: Text('Offers')),
          ],
          elevation: 8,
          onTap: _onNavTap,
        ),
      ),
    );
  }

  void _onNavTap(int value) {
    switch (value) {
      case 0:
        Navigator.push(
            context,
            SlideRightRoute(
              widget: NetworkAccountsPage(),
            ));
        break;
      case 1:
        Navigator.push(
            context,
            SlideRightRoute(
              widget: InvoicesPage(),
            ));
        break;
      case 2:
        break;
    }
  }

  Future<bool> doNothing() async {
    return false;
  }

  void _changeAccount() async {
    var result = await Navigator.push(
        context,
        SlideRightRoute(
          widget: NetworkAccountsPage(),
        ));
    if (result != null) {
      print(result);
      account = result as AccountInfo;
      await Prefs.saveAccount(account);
      var auth = FirebaseAuth.instance;
      await auth.signInAnonymously();
      nodes.forEach((n) async {
        if (account.name == n.addresses.elementAt(0)) {
          await Prefs.saveNode(n);
        }
      });
      print(
          '🍊 🍊 🍊 🍊 Signed in FRESH (anonymous) to Firebase: ${result.toString()}');
      _refresh();
    }
  }
}

class Content {
  String label, number;
  Color color;
  Icon icon;

  Content({this.label, this.number, this.color, this.icon});
}
