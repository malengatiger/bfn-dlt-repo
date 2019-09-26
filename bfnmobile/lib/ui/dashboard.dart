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
  List<AccountInfo> accounts = List(), accountMessages = List();
  List<Invoice> invoices = List(),
      invoiceMessages = List(),
      myInvoices = List();
  List<InvoiceOffer> offers = List(), offerMessages = List(), myOffers = List();
  AccountInfo account;
  List<NodeInfo> nodes = List();
  NodeInfo nodeInfo;

  @override
  void initState() {
    super.initState();
    _firebaseCloudMessaging();
    _getNodes();
    _refresh();
  }

  _getNodes() async {
    nodes = await Net.listNodes();
    nodeInfo = await Prefs.getNode();
    setState(() {});
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
        if (data['invoiceOffer'] != null) {
          var offer = json.decode(data['invoiceOffer']);
          var m = InvoiceOffer.fromJson(offer);
          offerMessages.add(m);
          _showMessage(
              'New Invoice Offer, amount: ' + m.offerAmount.toString());
        }
        if (data['invoice'] != null) {
          var offer = json.decode(data['invoice']);
          var m = Invoice.fromJson(offer);
          invoiceMessages.add(m);
          _showMessage('New Invoice, amount: ' + m.totalAmount.toString());
        }
        if (data['account'] != null) {
          var offer = json.decode(data['account']);
          var m = AccountInfo.fromJson(offer);
          accountMessages.add(m);
          _showMessage('New Account, name: ' + m.name.toString());
        }
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
    _firebaseMessaging.subscribeToTopic('accounts');
    print(
        '🧩🧩🧩🧩🧩🧩 subscribed to FCM topics 🍊 invoiceOffers 🍊 invoices 🍊 accounts');
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
        number: accounts.length,
        icon: Icon(Icons.people),
        backgroundColor: Colors.grey[300],
        textColor: Colors.teal[700]));
    setState(() {});
  }

  _getMyData() async {
    account = await Prefs.getAccount();
    myInvoices = await bfnBloc.getInvoices(accountId: account.identifier);
    contents.add(Content(
        label: 'My Invoices',
        number: myInvoices.length,
        icon: Icon(Icons.account_balance),
        textColor: Colors.blue));
    myOffers = await bfnBloc.getInvoiceOffers(accountId: account.identifier);
    contents.add(Content(
        label: 'My Offers',
        number: myOffers.length,
        icon: Icon(Icons.apps),
        textColor: Colors.pink));
    setState(() {});
  }

  _getNodeData() async {
    invoices = await bfnBloc.getInvoices();
    contents.add(Content(
        label: 'Node Invoices',
        number: invoices.length,
        icon: Icon(Icons.account_balance),
        backgroundColor: Colors.grey[300],
        textColor: Colors.black));
    offers = await bfnBloc.getInvoiceOffers();
    contents.add(Content(
        label: 'Node Offers',
        number: offers.length,
        icon: Icon(Icons.apps),
        backgroundColor: Colors.grey[300],
        textColor: Colors.black));
    setState(() {});
  }

  _refresh() async {
    setState(() {
      contents.clear();
    });
    await _getMyData();
    await _getNodeData();
    await _getAccounts();
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
                    height: 8,
                  ),
                  Text(
                    nodeInfo == null ? '' : nodeInfo.addresses.elementAt(0),
                    style: Styles.whiteSmall,
                  ),
                  SizedBox(
                    height: 20,
                  ),
                ],
              ),
              preferredSize: Size.fromHeight(80)),
        ),
        backgroundColor: Colors.brown[100],
        body: GridView.builder(
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2, mainAxisSpacing: 2, crossAxisSpacing: 2),
          itemCount: contents.length,
          itemBuilder: (BuildContext context, int index) {
            var content = contents.elementAt(index);
            return Padding(
              padding: const EdgeInsets.all(4.0),
              child: Container(
                height: 80,
                width: 160,
                child: Card(
                  color: content.backgroundColor == null
                      ? Colors.white
                      : content.backgroundColor,
                  elevation: 4,
                  child: Center(
                    child: Column(
                      children: <Widget>[
                        SizedBox(
                          height: 24,
                        ),
                        content.icon,
                        SizedBox(
                          height: 24,
                        ),
                        Text(
                          '${content.number}',
                          style: TextStyle(
                              fontSize: content.number > 1000 ? 36 : 44,
                              fontWeight: FontWeight.w900,
                              color: content.textColor),
                        ),
                        SizedBox(
                          height: 8,
                        ),
                        Text(content.label),
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
      print('account name: ${account.host} vs ');
      nodes.forEach((n) async {
        print('compare to: ${n.addresses.elementAt(0)}');
        if (account.host == n.addresses.elementAt(0)) {
          await Prefs.saveNode(n);
          setState(() {
            nodeInfo = n;
          });
        }
      });
      print(
          '🍊 🍊 🍊 🍊 Signed in FRESH (anonymous) to Firebase: ${result.toString()}');
      _refresh();
    }
  }
}

class Content {
  String label;
  int number;
  Color textColor, backgroundColor;
  Icon icon;

  Content(
      {this.label,
      this.number,
      this.textColor,
      this.icon,
      this.backgroundColor});
}
