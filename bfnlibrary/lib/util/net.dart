import 'package:bfnlibrary/util/local_storage.dart';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
class Net {

  static const URL = 'http://localhost:10411/';
//  static const URL = 'https://jsonplaceholder.typicode.com/posts/2';

  static Future<String> getAccounts() async {
    final response = await http.get(URL + 'admin/getAccounts');

    if (response.statusCode == 200) {
      print('🍎 🍊 Net: getAccounts: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return response.body;
    } else {
      throw Exception(' 👿  Failed: getAccounts');
    }
  }
  static Future<String> getInvoices() async {
    final response = await http.get(URL + 'admin/getInvoiceStates');

    if (response.statusCode == 200) {
      print('🍎 🍊 Net: getInvoices: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return response.body;
    } else {
      throw Exception(' 👿  Failed : getInvoices');
    }
  }
  static Future<String> getInvoiceOffers() async {
    final response = await http.get(URL + 'admin/getInvoiceOfferStates');

    if (response.statusCode == 200) {
      print('🍎 🍊 Net: getInvoiceOffers: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return response.body;
    } else {
      throw Exception(' 👿  Failed: getInvoiceOffers');
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
