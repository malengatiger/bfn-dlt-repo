import 'file:///Users/aubs/WORK/CORDA/bfn-dlt-repo/bfnmobile/lib/bloc.dart';
import 'package:bfnmobile/ui/network_accounts.dart';
import 'package:bfnmobile/ui/dashboard.dart';
import 'package:bfnmobile/ui/sign_up.dart';
import 'package:flutter/material.dart';
import 'package:bfnlibrary/util/slide_right.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'BFNApp',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.pink,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    _startData();
  }

  void _startData() async {
    var isAuthed = await bfnBloc.isUserAuthenticated();
    if (!isAuthed) {
      Navigator.push(context, SlideRightRoute(
        widget: SignUp(),
      ));
    } else {
      Navigator.push(context, SlideRightRoute(
        widget: Dashboard(),
      ));
    }
  }
  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(

          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              'You have pushed the button this many times:',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.display1,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
