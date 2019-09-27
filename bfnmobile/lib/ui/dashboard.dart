import 'dart:convert';
import 'dart:io';

import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/dashboard_data.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/data/node_info.dart';
import 'package:bfnlibrary/util/functions.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:bfnlibrary/util/prefs.dart';
import 'package:bfnlibrary/util/slide_right.dart';
import 'package:bfnlibrary/util/snack.dart';
import 'package:bfnlibrary/util/theme_bloc.dart';
import 'package:bfnlibrary/util/theme_util.dart';
import 'package:bfnmobile/bloc.dart';
import 'package:bfnmobile/ui/buy_offer.dart';
import 'package:bfnmobile/ui/list_tabs.dart';
import 'package:bfnmobile/ui/network_accounts.dart';
import 'package:dynamic_theme/dynamic_theme.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';

class Dashboard extends StatefulWidget {
  @override
  _DashboardState createState() => _DashboardState();
}

class _DashboardState extends State<Dashboard> {
  var _key = GlobalKey<ScaffoldState>();
  ThemeChanger themeChanger;
  FirebaseMessaging _firebaseMessaging = FirebaseMessaging();
  List<AccountInfo> accountMessages = List();
  List<Invoice> invoiceMessages = List(), myInvoices = List();
  List<InvoiceOffer> offerMessages = List(), myOffers = List();
  AccountInfo account;
  List<NodeInfo> nodes = List();
  NodeInfo nodeInfo;
  DashboardData data;

  @override
  void initState() {
    super.initState();
    _firebaseCloudMessaging();
    _getNodes();
    _refresh();

    //
  }

  void changeBrightness() {
    DynamicTheme.of(context).setBrightness(
        Theme.of(context).brightness == Brightness.dark
            ? Brightness.light
            : Brightness.dark);
  }

  void _changeTheme() {
    themeBloc.changeToRandomTheme();
  }

  _getDashboardData() async {
    data = await bfnBloc.getDashboardData();
    contents.add(Content(
        label: 'Node Invoices',
        number: data.invoices,
        icon: Icon(Icons.account_balance),
        backgroundColor: Colors.grey[300],
        textColor: Colors.black));
    contents.add(Content(
        label: 'Node Offers',
        number: data.offers,
        icon: Icon(Icons.apps),
        backgroundColor: Colors.grey[300],
        textColor: Colors.black));
    contents.add(Content(
        label: 'Network Accounts',
        number: data.accounts,
        icon: Icon(Icons.people),
        backgroundColor: Colors.grey[300],
        textColor: Colors.teal[700]));
    setState(() {});
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
      print("FCM user token :: $token");
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
          bfnBloc.addFCMInvoiceOffer(m, context);
        }
        if (data['invoice'] != null) {
          var invJson = json.decode(data['invoice']);
          var m = Invoice.fromJson(invJson);
          invoiceMessages.add(m);
          _showMessage('New Invoice, amount: ' + m.totalAmount.toString());
          bfnBloc.addFCMInvoice(m, context);
        }
        if (data['account'] != null) {
          var offer = json.decode(data['account']);
          var m = AccountInfo.fromJson(offer);
          accountMessages.add(m);
          _showMessage('New Account, name: ' + m.name.toString());
          bfnBloc.addFCMAccount(m, context);
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

  _refresh() async {
    if (account == null) {
      account = await Prefs.getAccount();
    }
    contents.clear();
    await _getMyData();
    data = await _getDashboardData();
  }

  List<Content> contents = List();
  String message;

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () => doNothing(),
      child: Scaffold(
        key: _key,
        appBar: AppBar(
          leading: IconButton(
            icon: Icon(Icons.brightness_7),
            onPressed: _changeTheme,
          ),
          title: Text("BFN"),
          elevation: 8,
          actions: <Widget>[
            IconButton(
              icon: Icon(Icons.refresh),
              onPressed: _refresh,
            ),
            IconButton(
              icon: Icon(Icons.person_add),
              onPressed: () {
                _changeAccount();
              },
            ),
          ],
          bottom: PreferredSize(
              child: Padding(
                padding: const EdgeInsets.only(left: 8, right: 8),
                child: Column(
                  children: <Widget>[
                    account == null
                        ? Container()
                        : NameBadge(
                            account: account,
                            nameStyle: Styles.whiteBoldMedium,
                            nodeStyle: Styles.whiteSmall,
                            elevation: 2,
                          ),
                    SizedBox(
                      height: 12,
                    ),
                    Padding(
                      padding: const EdgeInsets.only(left: 8.0),
                      child: Row(
                        children: <Widget>[
                          SizedBox(
                            width: 8,
                          ),
                          StreamBuilder<String>(
                              stream: bfnBloc.fcmStream,
                              initialData: 'No network message yet',
                              builder: (context, snapshot) {
                                if (snapshot.hasData) {
                                  debugPrint(
                                      ' 😡  😡  😡  😡  FCM message arrived on Stream: ${snapshot.data}  😡  😡  😡  😡 ');
                                  message = snapshot.data;
                                }
                                return Text(
                                  '$message',
                                  style: Styles.whiteSmall,
                                );
                              }),
                        ],
                      ),
                    ),
                    SizedBox(
                      height: 24,
                    ),
                  ],
                ),
              ),
              preferredSize: Size.fromHeight(120)),
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
