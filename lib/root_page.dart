import 'package:flutter/material.dart';
import 'package:gigu_poc/home_page.dart';
import 'package:gigu_poc/offer_simulator.dart';

class RootPage extends StatefulWidget {
  const new({super.key});

  @override
  State<RootPage> createState() => _RootPageState();
}

class _RootPageState extends State<RootPage> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _index,
        children: [HomePage(), OfferSimulator()],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.dashboard), label: 'Painel'),
          NavigationDestination(icon: Icon(Icons.local_taxi), label: 'Simulador'),
        ],
      ),
    );
  }
}
