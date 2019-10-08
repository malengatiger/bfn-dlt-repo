import 'dart:async';

import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/dashboard_data.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/functions.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:bfnlibrary/util/prefs.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/cupertino.dart';

BFNBloc bfnBloc = BFNBloc();

class BFNBloc {
  StreamController<List<AccountInfo>> _acctController =
      StreamController.broadcast();
  StreamController<List<Invoice>> _invoiceController =
      StreamController.broadcast();
  StreamController<List<InvoiceOffer>> _offerController =
      StreamController.broadcast();

  StreamController<String> _fcmController = StreamController.broadcast();
  StreamController<Invoice> _invoiceFCMController =
      StreamController.broadcast();
  StreamController<InvoiceOffer> _offerFCMController =
      StreamController.broadcast();

  StreamController<DashboardData> _dashController =
      StreamController.broadcast();

  Stream get fcmStream => _fcmController.stream;
  Stream get accountStream => _acctController.stream;
  Stream get invoiceStream => _invoiceFCMController.stream;
  Stream get offerStream => _offerFCMController.stream;
  Stream get dashboardStream => _dashController.stream;
  FirebaseAuth auth = FirebaseAuth.instance;
  FirebaseUser _user;
  AccountInfo account;

  BFNBloc() {
    getMyAccount();
  }
  Future<AccountInfo> getMyAccount() async {
    account = await Prefs.getAccount();
    return account;
  }

  close() {
    _acctController.close();
    _invoiceController.close();
    _offerController.close();
    _dashController.close();
    _fcmController.close();
    _invoiceFCMController.close();
    _offerFCMController.close();
  }

  void addFCMInvoice(Invoice invoice, BuildContext context) {
    debugPrint(
        '🥬 🥬 🥬 Putting arrived FCM message on stream: 🥬 invoice: ${invoice.invoiceNumber}');
    var msg =
        '🥬 Invoice added to Network ${getFormattedDateShortWithTime(invoice.dateRegistered, context)}';
    _fcmController.sink.add(msg);
    _invoiceFCMController.sink.add(invoice);
  }

  void addFCMInvoiceOffer(InvoiceOffer invoiceOffer, BuildContext context) {
    debugPrint(
        '🥬 🥬 🥬 Putting arrived FCM message on stream: 🥬 invoiceOffer: ${invoiceOffer.invoiceId}');
    var msg =
        '🍎 Offer added to Network ${getFormattedDateShortWithTime(invoiceOffer.offerDate, context)}';
    _fcmController.sink.add(msg);
    _offerFCMController.sink.add(invoiceOffer);
  }

  void addFCMAccount(AccountInfo account, BuildContext context) {
    debugPrint(
        '🥬 🥬 🥬 Putting arrived FCM message on stream: 🥬 account: ${account.name}');
    var msg = '🧩 Account added to Network: ${account.name}';
    _fcmController.sink.add(msg);
    _accounts.add(account);
    _acctController.sink.add(_accounts);
  }

  Future<bool> isUserAuthenticated() async {
    var user = await auth.currentUser();
    if (user == null) {
      debugPrint('🍎 🍎 🍊 User NOT authenticated! 🍎');
      return false;
    } else {
      debugPrint('🥬 🥬 🥬 User authenticated already 🥬 🥬 🥬 ');
      account = await Prefs.getAccount();
      return true;
    }
  }

  Future<FirebaseUser> signIn(String email, String password) async {
    var result =
        await auth.signInWithEmailAndPassword(email: email, password: password);
    if (result.user == null) {
      throw Exception('User sigin failed');
    }
    print('🥬 🥬 🥬 User successfully signed in: 🍎 🍊 ${result.user.uid}');
    _user = result.user;
    return _user;
  }

  List<AccountInfo> _accounts = List();
  Future<List<AccountInfo>> getAccounts() async {
    try {
      _accounts = await Net.getAccounts();
      print(
          '🍏 🍏 BFNBloc: getAccounts found 🔆 ${_accounts.length} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
      _acctController.sink.add(_accounts);
      return _accounts;
    } catch (e) {
      print(e);
      return List<AccountInfo>();
    }
  }

  Future<List<Invoice>> getInvoices({String accountId}) async {
    List<Invoice> invoices = List();
    if (accountId == null) {
      invoices = await Net.getInvoices();
    } else {
      invoices = await Net.getInvoices(accountId: accountId);
    }

    print(
        '🍏 🍏 BFNBloc: getInvoices found 🔆 ${invoices.length} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
    _invoiceController.sink.add(invoices);
    return invoices;
  }

  Future<List<InvoiceOffer>> getInvoiceOffers(
      {String accountId, bool consumed}) async {
    List<InvoiceOffer> offers = List();
    if (accountId == null) {
      offers = await Net.getInvoiceOffers();
    } else {
      offers =
          await Net.getInvoiceOffers(accountId: accountId, consumed: consumed);
    }

    print(
        '🍏 🍏 BFNBloc: getInvoiceOffers found 🔆 ${offers.length} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
    _offerController.sink.add(offers);
    return offers;
  }

  Future<DashboardData> getDashboardData() async {
    var data = await Net.getDashboardData();
    print(
        '🍏 🍏 BFNBloc: getDashboardData found 🔆 ${data.toJson()} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
    _dashController.sink.add(data);
    return data;
  }
}
