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
  StreamController<List<AccountInfo>> acctController =
      StreamController.broadcast();
  StreamController<List<Invoice>> invoiceController =
      StreamController.broadcast();
  StreamController<List<InvoiceOffer>> offerController =
      StreamController.broadcast();

  StreamController<String> fcmController = StreamController.broadcast();
  StreamController<Invoice> invoiceFCMController = StreamController.broadcast();
  StreamController<InvoiceOffer> offerFCMController =
      StreamController.broadcast();

  StreamController<DashboardData> dashController = StreamController.broadcast();

  Stream get fcmStream => fcmController.stream;
  FirebaseAuth auth = FirebaseAuth.instance;
  FirebaseUser user;
  AccountInfo account;

  BFNBloc() {
    getMyAccount();
  }
  Future<AccountInfo> getMyAccount() async {
    account = await Prefs.getAccount();
    return account;
  }

  close() {
    acctController.close();
    invoiceController.close();
    offerController.close();
    dashController.close();
    fcmController.close();
    invoiceFCMController.close();
    offerFCMController.close();
  }

  void addFCMInvoice(Invoice invoice, BuildContext context) {
    debugPrint(
        '🥬 🥬 🥬 Putting arrived FCM message on stream: 🥬 invoice: ${invoice.invoiceNumber}');
    var msg =
        '🥬 Invoice added to Network ${getFormattedDateShortWithTime(invoice.dateRegistered, context)}';
    fcmController.sink.add(msg);
  }

  void addFCMInvoiceOffer(InvoiceOffer invoiceOffer, BuildContext context) {
    debugPrint(
        '🥬 🥬 🥬 Putting arrived FCM message on stream: 🥬 invoiceOffer: ${invoiceOffer.invoiceId}');
    var msg =
        '🍎 Offer added to Network ${getFormattedDateShortWithTime(invoiceOffer.offerDate, context)}';
    fcmController.sink.add(msg);
  }

  void addFCMAccount(AccountInfo account, BuildContext context) {
    debugPrint(
        '🥬 🥬 🥬 Putting arrived FCM message on stream: 🥬 account: ${account.name}');
    var msg = '🧩 Account added to Network: ${account.name}';
    fcmController.sink.add(msg);
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
    user = result.user;
    return user;
  }

  Future<List<AccountInfo>> getAccounts() async {
    try {
      var accounts = await Net.getAccounts();
      print(
          '🍏 🍏 BFNBloc: getAccounts found 🔆 ${accounts.length} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
      acctController.sink.add(accounts);
      return accounts;
    } catch (e) {
      print(e);
      return List<AccountInfo>();
    }
  }

  Future<List<Invoice>> getInvoices({String accountId}) async {
    var invoices = await Net.getInvoices(accountId: accountId);
    print(
        '🍏 🍏 BFNBloc: getInvoices found 🔆 ${invoices.length} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
    invoiceController.sink.add(invoices);
    return invoices;
  }

  Future<List<InvoiceOffer>> getInvoiceOffers(
      {String accountId, bool consumed}) async {
    var offers =
        await Net.getInvoiceOffers(accountId: accountId, consumed: consumed);
    print(
        '🍏 🍏 BFNBloc: getInvoiceOffers found 🔆 ${offers.length} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
    offerController.sink.add(offers);
    return offers;
  }

  Future<DashboardData> getDashboardData() async {
    var data = await Net.getDashboardData();
    print(
        '🍏 🍏 BFNBloc: getDashboardData found 🔆 ${data.toJson()} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
    dashController.sink.add(data);
    return data;
  }
}
