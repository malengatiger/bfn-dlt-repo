import 'dart:async';
import 'dart:convert';

import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/cupertino.dart';

BFNBloc bfnBloc = BFNBloc();

class BFNBloc {

  StreamController<List<AccountInfo>> acctController = StreamController.broadcast();
  StreamController<List<Invoice>> invoiceController = StreamController.broadcast();
  StreamController<List<InvoiceOffer>> offerController = StreamController.broadcast();
  FirebaseAuth auth = FirebaseAuth.instance;
  FirebaseUser user;

  close() {
    acctController.close();
    invoiceController.close();
    offerController.close();
  }

  Future<bool> isUserAuthenticated() async {
    var user = await auth.currentUser();
    if (user == null) {
      debugPrint('🍎 🍎 🍊 User NOT authenticated! 🍎');
      return false;
    } else {
      debugPrint('🥬 🥬 🥬 User authenticated already 🥬 🥬 🥬 ');
      return true;
    }
     
  }
  Future<FirebaseUser> signIn( String email, String password) async {

    var result = await auth.signInWithEmailAndPassword(email: email, password: password);
    if (result.user == null) {
      throw Exception('User sigin failed');
    }
    print('🥬 🥬 🥬 User successfully signed in: 🍎 🍊 ${result.user.uid}');
    user = result.user;
    return user;
  }

  Future<List<AccountInfo>> getAccounts() async {
    var accounts = await Net.getAccounts();
    print('🍏 🍏 BFNBloc: getAccounts found 🔆 ${accounts.length} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
    acctController.sink.add(accounts);
    return accounts;
  }
  Future<List<Invoice>> getInvoices() async {
    var invoices = await Net.getInvoices();
    print('🍏 🍏 BFNBloc: getInvoices found 🔆 ${invoices.length} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
    invoiceController.sink.add(invoices);
    return invoices;
  }
  Future<List<InvoiceOffer>> getInvoiceOffers() async {
    var offers = await Net.getInvoiceOffers();
    print('🍏 🍏 BFNBloc: getInvoiceOffers found 🔆 ${offers.length} 🔆 🍏 🍏  - adding to stream 🧩 🧩 ');
    offerController.sink.add(offers);
    return offers;
  }
}
