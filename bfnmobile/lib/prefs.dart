import 'dart:convert';

import 'package:bfnlibrary/data/account.dart';
import 'package:shared_preferences/shared_preferences.dart';



class Prefs {

  static Future saveAccount(AccountInfo account) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();

    Map map = account.toJson();
    var jx = json.encode(map);
    prefs.setString('account', jx);
    print(
        "🌽 🌽 🌽 Account:  SAVED: 🌽: $jx ");
    return null;
  }

  static Future<AccountInfo> getAccount() async {
    var prefs = await SharedPreferences.getInstance();
    var string = prefs.getString('account');
    if (string == null) {
      return null;
    }
    var jx = json.decode(string);
    var association = new AccountInfo.fromJson(jx);
    print(
        "🌽 🌽 🌽 Account: retrieved : 🧩 $jx");
    return association;
  }
}
