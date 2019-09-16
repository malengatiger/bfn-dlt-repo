import 'dart:async';
import 'dart:convert';

import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/net.dart';

BFNBloc bfnBloc = BFNBloc();

class BFNBloc {

  StreamController<List<AccountInfo>> acctController = StreamController.broadcast();
  StreamController<List<Invoice>> invoiceController = StreamController.broadcast();
  StreamController<List<InvoiceOffer>> offerController = StreamController.broadcast();

  close() {
    acctController.close();
    invoiceController.close();
    offerController.close();
  }

  getAccounts() async {
    var res = await Net.getAccounts();
    List list = json.decode(res);
    var accounts = List<AccountInfo>();
    list.forEach((m) {
      accounts.add(AccountInfo.fromJson(m));
    });
    print('🍏 🍏 getAccounts found ${accounts.length} 🍏 🍏  - adding to stream');
    acctController.sink.add(accounts);
  }
  getInvoices() async {
    var res = await Net.getInvoices();
    List list = json.decode(res);
    var invoices = List<Invoice>();
    list.forEach((m) {
      invoices.add(Invoice.fromJson(m));
    });
    print('🍏 🍏 getInvoices found ${invoices.length} 🍏 🍏  - adding to stream');
    invoiceController.sink.add(invoices);
  }
  getInvoiceOffers() async {
    var res = await Net.getInvoiceOffers();
    List list = json.decode(res);
    var offers = List<InvoiceOffer>();
    list.forEach((m) {
      offers.add(InvoiceOffer.fromJson(m));
    });
    print('🍏 🍏 getInvoiceOffers found ${offers.length} 🍏 🍏  - adding to stream');
    offerController.sink.add(offers);
  }
}
