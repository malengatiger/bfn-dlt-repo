import 'dart:convert';

import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/dashboard_data.dart';
import 'package:bfnlibrary/data/fb_user.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/data/node_info.dart';
import 'package:bfnlibrary/util/prefs.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:http/http.dart' as http;

class Net {
  static Firestore db = Firestore.instance;
  static FirebaseAuth auth = FirebaseAuth.instance;
  static const Map<String, String> headers = {
    'Content-type': 'application/json',
    'Accept': 'application/json',
  };

  static Future _getCachedURL() async {
    var url = await Prefs.getUrl();
    return url;
  }

  static Future<List<NodeInfo>> listNodes() async {
    var list = List<NodeInfo>();
    var result = await auth.currentUser();
    if (result == null) {
      var email = DotEnv().env['email'];
      var pass = DotEnv().env['password'];
      print('🌸 🌸 🌸 🌸 🌸 email from .env : 🌸  $email 🌸  pass: $pass');
      var userResult =
          await auth.signInWithEmailAndPassword(email: email, password: pass);
      print(
          '🍊 🍊 🍊 Logged into Firebase with .env credentials,  🌸 uid: ${userResult.user.uid} ... getting nodes ...');
      list = await _getNodes(list);
      await auth.signOut();
      print('🍊 🍊 🍊 Logged OUT of Firebase  ${userResult.user.uid} ... ');
    } else {
      list = await _getNodes(list);
    }
    if (list.isNotEmpty) {
      await Prefs.saveNodes(list);
    }
    return list;
  }

  static Future<String> getNodeUrl() async {
    var m = await _getCachedURL();
    if (m != null) {
      return m;
    }
    var acct = await Prefs.getAccount();
    if (acct == null) {
      throw Exception("Account not available yet");
    }
    var list = await listNodes();
    String url;
    print('  🔆  🔆  🔆 local account:  💚 ${acct.toJson()}');
    list.forEach((node) {
      var host = node.addresses.elementAt(0);
      print('  🔆  🔆  🔆 host of node:  💚 $host');
      if (host == acct.host) {
        url = node.webAPIUrl;
      }
    });
    if (url == null) {
      throw Exception("Url not found");
    }
    Prefs.setUrl(url);
    return url;
  }

  static Future _getNodes(List<NodeInfo> list) async {
    var snapshot = await db.collection("nodes").getDocuments();
    print('nodes found on network: ${snapshot.documents.length}');
    snapshot.documents.forEach((doc) {
      var data = doc.data;
      print('data from Firestore: $data');
      var node = NodeInfo.fromJson(data);
      list.add(node);
    });
    return list;
  }

  static Future<String> get(String mUrl) async {
    var start = DateTime.now();
    var client = new http.Client();
    var resp = await client
        .get(
      mUrl,
      headers: headers,
    )
        .whenComplete(() {
      client.close();
    });

    var end = DateTime.now();
    debugPrint(
        '🍎 🍊 Net: post  ##################### elapsed: ${end.difference(start).inSeconds} seconds\n\n');
    if (resp.statusCode == 200) {
      debugPrint(
          '🍎 🍊 Net: get: SUCCESS: Network Response Status Code: 🥬  🥬 ${resp.statusCode} 🥬  $mUrl');
      return resp.body;
    } else {
      var msg = ' 👿  Failed status code: ${resp.statusCode} 🥬  $mUrl';
      debugPrint(msg);
      throw Exception(msg);
    }
  }

  static Future post(String mUrl, Map bag) async {
    var start = DateTime.now();
    var client = new http.Client();
    String body;
    if (bag != null) {
      body = json.encode(bag);
    }
    debugPrint('🍊 🍊 🍊 Net: post ... calling with bag: $body');
    var resp = await client
        .post(
      mUrl,
      body: body,
      headers: headers,
    )
        .whenComplete(() {
      debugPrint('🍊 🍊 🍊 Net: post whenComplete ');
      client.close();
    });
    print(resp.body);
    var end = DateTime.now();
    debugPrint(
        '🍎 🍊 Net: post  ##################### elapsed: ${end.difference(start).inSeconds} seconds\n\n');
    if (resp.statusCode == 200) {
      debugPrint(
          '🍎 🍊 Net: post: SUCCESS: Network Response Status Code: 🥬  🥬 ${resp.statusCode} 🥬  $mUrl');
      return resp.body;
    } else {
      var msg = ' 👿  Failed status code: ${resp.statusCode} 🥬  $mUrl';
      debugPrint(resp.body);
      throw Exception(msg);
    }
  }

  static Future<AccountInfo> startAccountRegistrationFlow(
      String name, String email, String password, String cellphone) async {
    var bag = {
      "name": name,
      "email": email,
      "password": password,
      "cellphone": cellphone
    };
    debugPrint('🍊🍊🍊🍊🍊 startAccountRegistrationFlow starting the call ...');
    var node = await Prefs.getNode();
    final response =
        await post(node.webAPIUrl + 'admin/startAccountRegistrationFlow', bag);
    var m = json.decode(response);
    var acct = AccountInfo.fromJson(m);
    return acct;
  }

  static Future<Invoice> startRegisterInvoiceFlow(Invoice invoice) async {
    var node = await Prefs.getNode();
    final response = await post(
        node.webAPIUrl + 'supplier/startRegisterInvoiceFlow', invoice.toJson());
    var m = json.decode(response);
    var acct = Invoice.fromJson(m);
    return acct;
  }

  static Future<String> buyInvoiceOffer(String invoiceId) async {
    var user = await Prefs.getAccount();
    var node = await Prefs.getNode();
    final response = await get(node.webAPIUrl +
        'investor/buyInvoiceOffer?invoiceId=$invoiceId&investorId=${user.identifier}');
    return response;
  }

  static Future<InvoiceOffer> startInvoiceOfferFlow(
      InvoiceOffer invoiceOffer) async {
    var node = await Prefs.getNode();
    final response = await post(
        node.webAPIUrl + 'supplier/startInvoiceOfferFlow',
        invoiceOffer.toJson());
    var m = json.decode(response);
    var acct = InvoiceOffer.fromJson(m);
    return acct;
  }

  static Future<List<AccountInfo>> getAccounts() async {
    var prefix = await getNodeUrl();
    final response = await get(prefix + 'admin/getAccounts');

    List<AccountInfo> list = List();
    List m = json.decode(response);
    m.forEach((f) {
      list.add(AccountInfo.fromJson(f));
    });
    debugPrint('🍎 🍊 Net: getAccounts: found ${list.length}');
    return list;
  }

  static Future<AccountInfo> getAccount(String accountId) async {
    var node = await Prefs.getNode();
    final response =
        await get(node.webAPIUrl + 'admin/getAccount?accountId=$accountId');

    AccountInfo acctInfo = AccountInfo.fromJson(json.decode(response));
    debugPrint('🍎 🍊 Net: getAccount: found ${acctInfo.toJson()}');
    return acctInfo;
  }

  static Future<UserRecord> getUser(String email) async {
    var node = await Prefs.getNode();
    String url = node.webAPIUrl + 'admin/getUser?email=$email';
    ;
    final response = await http.get(url);
    if (response.statusCode == 200) {
      debugPrint(
          '🍎 🍊 Net: getInvoices: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return UserRecord.fromJson(json.decode(response.body));
    } else {
      throw Exception(
          ' 👿  Failed : getUser Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
    }
  }

  static Future<List<Invoice>> getInvoices(
      {String accountId, bool consumed}) async {
    var node = await Prefs.getNode();
    if (consumed == null) consumed = false;
    String url;
    if (accountId == null) {
      url = node.webAPIUrl + 'admin/getInvoiceStates?consumed=$consumed';
    } else {
      url = node.webAPIUrl +
          'admin/getInvoiceStates?accountId=$accountId&consumed=$consumed';
    }
    debugPrint(url);
    final response = await get(url);

    List<Invoice> list = List();
    List m = json.decode(response);
    m.forEach((f) {
      list.add(Invoice.fromJson(f));
    });
    debugPrint('🍎 🍊 🍎 🍊 Net: getInvoices: found ${list.length}');
    return list;
  }

  static Future<List<InvoiceOffer>> getInvoiceOffers(
      {String accountId, bool consumed}) async {
    var node = await Prefs.getNode();
    if (consumed == null) consumed = false;
    String url;
    if (accountId == null) {
      url = node.webAPIUrl + 'admin/getInvoiceOfferStates?consumed=$consumed';
    } else {
      url = node.webAPIUrl +
          'admin/getInvoiceOfferStates?accountId=$accountId&consumed=$consumed';
    }
    debugPrint(url);
    final response = await get(url);

    List<InvoiceOffer> list = List();
    List m = json.decode(response);
    m.forEach((f) {
      list.add(InvoiceOffer.fromJson(f));
    });
    debugPrint('🍎 🍊 🍎 🍊 Net: getInvoiceOffers: found ${list.length}');
    return list;
  }

  static Future<DashboardData> getDashboardData() async {
    var node = await Prefs.getNode();
    String url = node.webAPIUrl + 'admin/getDashboardData';

    debugPrint(url);
    final response = await get(url);
    var data = DashboardData.fromJson(json.decode(response));
    debugPrint('🍎 🍊 🍎 🍊 Net: getDashboardData: found ${data.toJson()}');
    return data;
  }

  static Future<String> ping() async {
    var node = await Prefs.getNode();
    final response = await http.get(node.webAPIUrl + 'admin/ping');
    if (response.statusCode == 200) {
      debugPrint(
          '🍎 🍊 Net: ping: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return response.body;
    } else {
      throw Exception(' 👿  Failed ping');
    }
  }
//
//  static Future<String> startDemoDataGeneration() async {
//    final response = await http.get(URL + 'admin/demo');
//
//    if (response.statusCode == 200) {
//      debugPrint(
//          '🍎 🍊 Net: startDemoDataGeneration: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
//      Prefs.setDemoString('DEMO DATA COMPLETE');
//      return response.body;
//    } else {
//      throw Exception(' 👿  Failed: startDemoDataGeneration');
//    }
//  }
}
