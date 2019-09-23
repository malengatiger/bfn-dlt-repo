import 'dart:convert';

import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/fb_user.dart';
import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/local_storage.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:http/http.dart' as http;
class Net {

  static const URL = 'http://192.168.86.240:10416/';
//  static const URL = 'https://jsonplaceholder.typicode.com/posts/2';

  static const Map<String, String> headers = {
    'Content-type': 'application/json',
    'Accept': 'application/json',
  };

  static Future get(String mUrl) async {
    var start = DateTime.now();
    var client = new http.Client();
    var resp = await client
        .get(mUrl, headers: headers, )
        .whenComplete(() {
      client.close();
    });
   
    var end = DateTime.now();
    print(
        '🍎 🍊 Net: post  ##################### elapsed: ${end.difference(start).inSeconds} seconds\n\n');
    if (resp.statusCode == 200) {
      print('🍎 🍊 Net: get: SUCCESS: Network Response Status Code: 🥬  🥬 ${resp.statusCode} 🥬  $mUrl');
      return resp.body;
    } else {
      var msg = ' 👿  Failed status code: ${resp.statusCode} 🥬  $mUrl';
      print(msg);
      throw Exception(msg);
    }
    return resp;
  }
  static Future post(String mUrl, Map bag) async {
    var start = DateTime.now();
    var client = new http.Client();
    String body;
    if (bag != null) {
      body = json.encode(bag);
    }
    print('🍊 🍊 🍊 Net: post ... calling with bag: $bag');
    var resp = await client
        .post(mUrl, body: body,headers: headers, )
        .whenComplete(() {
      print('🍊 🍊 🍊 Net: post whenComplete ');
      client.close();
    });
    print(resp);
    var end = DateTime.now();
    print(
        '🍎 🍊 Net: post  ##################### elapsed: ${end.difference(start).inSeconds} seconds\n\n');
    if (resp.statusCode == 200) {
      print('🍎 🍊 Net: post: SUCCESS: Network Response Status Code: 🥬  🥬 ${resp.statusCode} 🥬  $mUrl');
      return resp.body;
    } else {
      var msg = ' 👿  Failed status code: ${resp.statusCode} 🥬  $mUrl';
      print(resp.body);
      throw Exception(msg);
    }
  }
  
  static Future<AccountInfo> startAccountRegistrationFlow(String name, String email, String password, String cellphone) async {
    var bag = {
      "name": name,
      "email": email,
      "password": password,
      "cellphone": cellphone
    };
    print('🍊🍊🍊🍊🍊 startAccountRegistrationFlow starting the call ...');
    final response = await post(URL + 'admin/startAccountRegistrationFlow', bag);
    var m = json.decode(response);
    var acct = AccountInfo.fromJson(m);
    return acct;
  }
  static Future<Invoice> startRegisterInvoiceFlow(Invoice invoice) async {

    final response = await post(URL + 'supplier/startRegisterInvoiceFlow', invoice.toJson());
    var m = json.decode(response);
    var acct = Invoice.fromJson(m);
    return acct;
  }
  static FirebaseAuth auth = FirebaseAuth.instance;
  static Future<String> buyInvoiceOffer(String invoiceId) async {
    var user = await auth.currentUser();
    var bag = {
      "invoiceId": invoiceId,
      "investorId": user.uid
    };
    final response = await post(URL + 'investor/buyInvoiceOffer', bag);
    return response;
  }
  static Future<InvoiceOffer> startInvoiceOfferFlow(InvoiceOffer invoiceOffer) async {
    final response = await post(URL + 'supplier/startInvoiceOfferFlow', invoiceOffer.toJson());
    var m = json.decode(response);
    var acct = InvoiceOffer.fromJson(m);
    return acct;
  }
  static Future<List<AccountInfo>> getAccounts() async {
    final response = await http.get(URL + 'admin/getAccounts');

    if (response.statusCode == 200) {
      print('🍎 🍊 Net: getAccounts: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      List<AccountInfo> list = List();
      List m = json.decode(response.body);
      m.forEach((f) {
        list.add(AccountInfo.fromJson(f));
      });
      print('🍎 🍊 Net: getAccounts: found ${list.length}');
      return list;
    } else {
      throw Exception(' 👿  Failed: getAccounts Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
    }
  }
  static Future<AccountInfo> getAccount(String accountId) async {
    final response = await http.get(URL + 'admin/getAccount?accountId=$accountId');

    if (response.statusCode == 200) {
      print('🍎 🍊 Net: getAccount: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      AccountInfo acctInfo = AccountInfo.fromJson(json.decode(response.body));
      print('🍎 🍊 Net: getAccount: found ${acctInfo.toJson()}');
      return acctInfo;
    } else {
      throw Exception(' 👿  Failed: getAccounts Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
    }
  }
  static Future<UserRecord> getUser(String email) async {
    String url = URL + 'admin/getUser?email=$email';;
    final response = await http.get(url);

    if (response.statusCode == 200) {
      print('🍎 🍊 Net: getInvoices: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return UserRecord.fromJson(json.decode(response.body));
    } else {
      throw Exception(' 👿  Failed : getUser Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
    }
  }
  static Future<List<Invoice>> getInvoices({String accountId, bool consumed}) async {
    if (consumed == null) consumed = false;
    String url;
    if (accountId == null) {
      url = URL + 'admin/getInvoiceStates?consumed=$consumed';
    } else {
      url = URL + 'admin/getInvoiceStates?accountId=$accountId&consumed=$consumed';
    }
    print(url);
    final response = await http.get(url);

    if (response.statusCode == 200) {
      print('🍎 🍊 Net: getInvoices: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      List<Invoice> list = List();
      List m = json.decode(response.body);
      m.forEach((f) {
        list.add(Invoice.fromJson(f));
      });
      print('🍎 🍊 🍎 🍊 Net: getInvoices: found ${list.length}');
      return list;
    } else {
      throw Exception(' 👿  Failed : getInvoices Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
    }
  }
  static Future<List<InvoiceOffer>> getInvoiceOffers({String accountId, bool consumed}) async {
    if (consumed == null) consumed = false;
    String url;
    if (accountId == null) {
      url = URL + 'admin/getInvoiceOfferStates?consumed=$consumed';
    } else {
      url = URL + 'admin/getInvoiceOfferStates?accountId=$accountId&consumed=$consumed';
    }
    print(url);
    final response = await http.get(url);

    if (response.statusCode == 200) {
      print('🍎 🍊 Net: getInvoiceOffers: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      List<InvoiceOffer> list = List();
      List m = json.decode(response.body);
      m.forEach((f) {
        list.add(InvoiceOffer.fromJson(f));
      });
      print('🍎 🍊 🍎 🍊 Net: getInvoiceOffers: found ${list.length}');
      return list;
    } else {
      throw Exception(' 👿  Failed: getInvoiceOffers Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
    }
  }
  static Future<String> ping() async {
    final response = await http.get(URL + 'admin/ping');

    if (response.statusCode == 200) {
      print('🍎 🍊 Net: ping: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return response.body;
    } else {
      throw Exception(' 👿  Failed ping');
    }

  }
  static Future<String> startDemoDataGeneration() async {
    final response = await http.get(URL + 'admin/demo');

    if (response.statusCode == 200) {
      print('🍎 🍊 Net: startDemoDataGeneration: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      Prefs.setDemoString('DEMO DATA COMPLETE');
      return response.body;
    } else {
      throw Exception(' 👿  Failed: startDemoDataGeneration');
    }

  }

}
