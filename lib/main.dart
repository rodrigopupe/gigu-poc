import 'package:flutter/material.dart';
import 'package:gigu_poc/root_page.dart';

void main() {
  runApp(const GiguApp());
}

class GiguApp extends StatelessWidget {
  const GiguApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'mini-GigU',
      theme: ThemeData(colorScheme: .fromSeed(seedColor: Colors.deepPurple)),
      home: const RootPage(),
    );
  }
}
