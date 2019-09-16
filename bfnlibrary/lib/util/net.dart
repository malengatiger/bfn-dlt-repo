import 'package:http/http.dart' as http;
class Net {

  static const URL = 'http://localhost:10411/';
//  static const URL = 'https://jsonplaceholder.typicode.com/posts/2';

  static Future<String> getAccounts() async {
    final response = await http.get(URL + 'admin/getAccounts');

    if (response.statusCode == 200) {
      print('🍏 🍎 🍐 🍊 getAccounts: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return response.body;
    } else {
      throw Exception('🍏 🍎 🍐 🍊 Failed to load post');
    }
  }
  static Future<String> getInvoices() async {
    final response = await http.get(URL + 'admin/getInvoiceStates');

    if (response.statusCode == 200) {
      print('🍏 🍎 🍐 🍊 getInvoices: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return response.body;
    } else {
      throw Exception('🍏 🍎 🍐 🍊 Failed to load post');
    }
  }
  static Future<String> getInvoiceOffers() async {
    final response = await http.get(URL + 'admin/getInvoiceOfferStates');

    if (response.statusCode == 200) {
      print('🍏 🍎 🍐 🍊 getInvoiceOffers: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return response.body;
    } else {
      throw Exception('🍏 🍎 🍐 🍊 Failed to load post');
    }
  }
  static Future<String> ping() async {
    final response = await http.get(URL + 'admin/ping');

    if (response.statusCode == 200) {
      print('🍏 🍎 🍐 🍊 ping: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
      return response.body;
    } else {
      throw Exception('🍏 🍎 🍐 🍊 Failed to load post');
    }
  }

}
